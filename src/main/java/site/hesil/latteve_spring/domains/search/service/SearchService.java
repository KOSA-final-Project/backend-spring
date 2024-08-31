package site.hesil.latteve_spring.domains.search.service;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer;
import org.springframework.stereotype.Service;
import site.hesil.latteve_spring.domains.job.domain.Job;
import site.hesil.latteve_spring.domains.member.domain.Member;
import site.hesil.latteve_spring.domains.member.domain.memberJob.MemberJob;
import site.hesil.latteve_spring.domains.member.repository.MemberRepository;
import site.hesil.latteve_spring.domains.member.repository.memberjob.MemberJobRepository;
import site.hesil.latteve_spring.domains.memberStack.domain.MemberStack;
import site.hesil.latteve_spring.domains.memberStack.repository.MemberStackRepository;
import site.hesil.latteve_spring.domains.project.domain.Project;
import site.hesil.latteve_spring.domains.project.repository.projectmember.ProjectMemberRepository;
import site.hesil.latteve_spring.domains.project.repository.project.ProjectRepository;
import site.hesil.latteve_spring.domains.project.repository.recruitment.RecruitmentRepository;
import site.hesil.latteve_spring.domains.projectStack.domain.ProjectStack;
import site.hesil.latteve_spring.domains.projectStack.repository.ProjectStackRepository;
import site.hesil.latteve_spring.domains.search.dto.member.request.MemberDocumentReq;
import site.hesil.latteve_spring.domains.search.dto.project.request.ProjectDocumentReq;
import site.hesil.latteve_spring.domains.techStack.domain.TechStack;
import site.hesil.latteve_spring.domains.techStack.repository.TechStackRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * packageName    : site.hesil.latteve_spring.domains.search.service
 * fileName       : SearchProjectService
 * author         : Heeseon
 * date           : 2024-08-28
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-08-28        Heeseon       최초 생성
 */

@Service
@RequiredArgsConstructor
public class SearchService {
    private final MemberRepository memberRepository;
    private final MemberStackRepository memberStackRepository;
    private final ProjectRepository projectRepository;
    private final OpenSearchClient openSearchClient;
    private final ProjectStackRepository projectStackRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final TechStackRepository techStackRepository;
    private final MemberJobRepository memberJobRepository;
    private final SortHandlerMethodArgumentResolverCustomizer sortCustomizer;

    // 나중에 삭제 -> 데이터 동기화
    @PostConstruct
    public void init() throws IOException {
        createOrRecreateIndexWithMapping("projects"); // projects 인덱스 생성 또는 재생성
        createOrRecreateIndexWithMapping("members");  // members 인덱스 생성 또는 재생성
        indexProjectsToOpenSearch();  // 프로젝트 데이터 인덱싱
        indexMembersToOpenSearch(); // 멤버 데이터 인덱싱
    }
    private void createOrRecreateIndexWithMapping(String indexName) throws IOException {
        // 인덱스 존재 여부 확인
        boolean exists = openSearchClient.indices().exists(e -> e.index(indexName)).value();

        if (exists) {
            // 인덱스가 존재할 경우 삭제
            openSearchClient.indices().delete(new DeleteIndexRequest.Builder().index(indexName).build());
        }

        // 인덱스별 techStack 매핑 설정
        TypeMapping mapping = createTechStackMapping(indexName);

        // 인덱스 생성 요청
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                .index(indexName)
                .mappings(mapping)
                .build();

        // 인덱스 생성
        CreateIndexResponse createIndexResponse = openSearchClient.indices().create(createIndexRequest);

        if (!createIndexResponse.acknowledged()) {
            throw new RuntimeException("Failed to create OpenSearch index with mappings.");
        }
    }


    private TypeMapping createTechStackMapping(String indexName) {
        String filedName;
        Property techStackProperty = Property.of(b -> b
                .nested(o -> o
                        .properties("key", Property.of(pb -> pb.text(t -> t)))
                        .properties("value", Property.of(pb -> pb.text(t -> t)))
                )
        );


        if("projects".equals(indexName)) {
            filedName = "projectTechStack";
        }else if("members".equals(indexName)){
            filedName = "techStack";
        }else {
            throw new IllegalArgumentException("Unknown index name: " + indexName);
        }
        // 매핑 생성
        return TypeMapping.of(mb -> mb
                .properties(filedName, techStackProperty)
        );
    }


    /** project search */
    @Transactional
    public void indexProjectsToOpenSearch() throws IOException {
        List<Project> projects = projectRepository.findAll();
        //Project -> Index
        for(Project project : projects) {
            // 프로젝트에 연관된 기술 스택  정보 가져옴
            List<ProjectStack> projectTechStacks = projectStackRepository.findAllByProject_ProjectId(project.getProjectId());

            // techStack list로 저장
            List<ProjectDocumentReq.TechStack> techStackList= new ArrayList<>();
            for (ProjectStack projectStack : projectTechStacks) {
                Optional<TechStack> techStackOpt = techStackRepository.findById(projectStack.getTechStack().getTechStackId());
                if (techStackOpt.isPresent()) {
                    TechStack techStack = techStackOpt.get();
                    String name = techStack.getName();
                    String imgUrl = techStack.getImgUrl() != null ? techStack.getImgUrl() :
                            (projectStack.getCustomStack() != null ? projectStack.getCustomStack() : name);
                    techStackList.add(new ProjectDocumentReq.TechStack(name, imgUrl));
                }
            }

            String statusToString = convertStatusToString(project.getStatus());


            // 프로젝트에 필요한 인원
            Integer requiredMemberCount = recruitmentRepository.findMemberCountByProject_ProjectId(project.getProjectId());
            // 프로젝트에 지원한 인원
            Integer currentMemberCount = projectMemberRepository.findMemberCountByProject_ProjectId(project.getProjectId());

            // ProjectDocumentReq 생성
            ProjectDocumentReq projectDocumentReq = ProjectDocumentReq.builder()
                    .name(project.getName())
                    .imgUrl(project.getImgUrl())
                    .duration(project.getDuration())
                    .projectTechStack(techStackList)
                    .teamCnt(requiredMemberCount)
                    .currentCnt(currentMemberCount)
                    .status(statusToString)
                    .createdAt( formatLocalDateTime(project.getCreatedAt()))
                    .build();

            // Elasticsearch에 인덱싱
            IndexRequest<ProjectDocumentReq> indexRequest = new IndexRequest.Builder<ProjectDocumentReq>()
                    .index("projects")
                    .id(project.getProjectId().toString())
                    .document(projectDocumentReq)
                    .build();
            openSearchClient.index(indexRequest);
        }
    }

    private String convertStatusToString(int status) {
        switch (status) {
            case 0: return "모집중";
            case 1: return "진행중";
            case 2: return "종료";
            default: return "Unknown";
        }
    }

    public List<ProjectDocumentReq> searchProjectsByKeyword(String keyword, String status, String sort) throws IOException {

        // keyword로 검색
        BoolQuery boolQuery = BoolQuery.of(b -> {
            // 프로젝트 이름에 대한 검색
            b.should(MatchQuery.of(m -> m.field("name").query(FieldValue.of(keyword)))._toQuery());
            //기술 스택 이름에 대한 검색
            b.should(NestedQuery.of(n -> n
                    .path("projectTechStack")  // nested 필드의 경로 지정
                    .query(q -> q
                            .match(m -> m
                                    .field("projectTechStack.name")
                                    .query(FieldValue.of(keyword))
                            )
                    )
            )._toQuery());
            // 최소 매칭 조건 : 이름이나 기술스택에 대해 하나라도 일치
            b.minimumShouldMatch(String.valueOf(1));

            // project 상태로 필터링
            if (status != null && !status.isEmpty()) {
                b.filter(TermQuery.of(t -> t.field("status").value(FieldValue.of(status)))._toQuery());
            }
            return b;
        });

        // SearchRequest 빌드
        SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                .index("projects")  // 검색할 인덱스 이름
                .query(boolQuery._toQuery());

        // 정렬 기준 설정
        if (sort != null && !sort.isEmpty()) {
            if ("cntLike".equalsIgnoreCase(sort)) {
                searchRequest.sort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("cntLike")  // 좋아요 수로 정렬
                                .order(SortOrder.Desc)  // 내림차순 (많이 받은 순)
                        )
                ));
            } else if ("latest".equalsIgnoreCase(sort)) {
                searchRequest.sort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("createdAt")  // 생성일자로 정렬
                                .order(SortOrder.Desc)  // 내림차순 (최신순)
                        )
                ));
            }
        }

        // OpenSearch 클라이언트를 사용하여 검색 요청 실행
        SearchResponse<ProjectDocumentReq> response = openSearchClient.search(searchRequest.build(), ProjectDocumentReq.class);

        // 검색 결과를 ProjectDocumentReq 리스트로 변환하여 반환
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    /**Member search */
    @Transactional
    public void indexMembersToOpenSearch() throws IOException {
        List<Member> members = memberRepository.findAll();
        // Member -> Index
        for (Member member : members) {
            // 멤버에 연관된 기술 스택 정보 가져옴
            List<MemberStack> memberStacks = memberStackRepository.findAllByMember_MemberId(member.getMemberId());
            // techStack list로 저장
            List<MemberDocumentReq.TechStack> techStackList = new ArrayList<>();
            for (MemberStack memberStack : memberStacks) {
                Optional<TechStack> techStackOpt = techStackRepository.findById(memberStack.getTechStack().getTechStackId());
                if (techStackOpt.isPresent()) {
                    TechStack techStack = techStackOpt.get();
                    String name = techStack.getName();
                    String imgUrl = techStack.getImgUrl() != null ? techStack.getImgUrl() : (memberStack.getCustomStack() != null ? memberStack.getCustomStack() : name);

                    // TechStack 객체를 리스트에 추가
                    techStackList.add(new MemberDocumentReq.TechStack(name, imgUrl));
                }
            }
            // 멤버의 직무 정보 가져옴
            List<MemberJob> memberJobs = memberJobRepository.findAllByMember_MemberId(member.getMemberId());

            List<String> jobList = new ArrayList<>();
            // 멤버의 직무 이름 list로 저장
            for(MemberJob memberJob : memberJobs) {
                jobList.add(memberJob.getJob().getName());
            }

            // MemberDocumentReq 생성
            MemberDocumentReq memberDocumentReq = MemberDocumentReq.builder()
                    .memberId(member.getMemberId())
                    .memberNickname(member.getNickname())
                    .memberImg(member.getImgUrl())
                    .memberGithub(member.getGithub())
                    .techStack(techStackList)  // TechStack 리스트 전달
                    .memberJob(jobList)
                    .createdAt(formatLocalDateTime(member.getCreatedAt()))
                    .build();

            // Elasticsearch에 인덱싱
            IndexRequest<MemberDocumentReq> indexRequest = new IndexRequest.Builder<MemberDocumentReq>()
                    .index("members")
                    .id(member.getMemberId().toString())
                    .document(memberDocumentReq)
                    .build();
            openSearchClient.index(indexRequest);
        }
    }

    public List<MemberDocumentReq> searchMembersByKeyword(String keyword , String sort) throws IOException {

        BoolQuery boolQuery = BoolQuery.of(b -> {
            // 라떼버 이름으로 검색
            b.should(MatchQuery.of(m -> m.field("nickname").query(FieldValue.of(keyword)))._toQuery());
            // 기술 스택 이름으로 검색
            b.should(NestedQuery.of(n -> n
                    .path("techStack")  // nested 필드의 경로 지정
                    .query(q -> q
                            .match(m -> m
                                    .field("techStack.name")
                                    .query(FieldValue.of(keyword))
                            )
                    )
            )._toQuery());
            // 경력으로 검색
            b.should(MatchQuery.of(m -> m.field("career").query(FieldValue.of(keyword)))._toQuery());
            // 최소 매칭 조건
            b.minimumShouldMatch(String.valueOf(1));



            return b;
        });

        // SearchRequest 빌더에 결합된 쿼리 추가
        SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                .index("members")  // 검색할 인덱스 이름
                .query( boolQuery._toQuery());  // 결합된 쿼리

        // 정렬 기준 설정
        if (sort != null && !sort.isEmpty()) {
            if ("latest".equalsIgnoreCase(sort)) {
                searchRequest.sort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("createdAt")  // 최신순 정렬
                                .order(SortOrder.Desc)  // 내림차순
                        )
                ));
            } else if ("career".equalsIgnoreCase(sort)) {
                searchRequest.sort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("career")  // 경력 기준 정렬
                                .order(SortOrder.Desc)  // 내림차순 (경력 많은 순)
                        )
                ));
            }
        } else {
            // 기본 정렬: 이름순 정렬 (오름차순)
            searchRequest.sort(SortOptions.of(s -> s
                    .field(f -> f
                            .field("nickname")  // 이름순 정렬
                            .order(SortOrder.Asc)  // 오름차순
                    )
            ));
        }

        SearchResponse<MemberDocumentReq> response = openSearchClient.search(searchRequest.build(), MemberDocumentReq.class);

        // 검색 결과를 MemberDocumentReq 리스트로 변환하여 반환
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    // 통합 검색 메서드
    public Map<String, Object> searchAllByKeyword(String keyword) throws IOException {
//        List<MemberDocumentReq> members = searchMembersByKeyword(keyword);
        Map<String, Object> result = new HashMap<>();
//        result.put("members", members);
//        result.put("projects", projects);

        return result;
    }

    //member에서 techstack keyword로 검색되는지 확인
    public List<MemberDocumentReq> searchMembersByTechStack(String techStackKey) throws IOException {
        // 특정 키 존재 여부 확인 쿼리 생성
        Query query = createTechStackExistsQuery(techStackKey);

        // SearchRequest 빌더에 결합된 쿼리 추가
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("members")  // 검색할 인덱스 이름
                .query(query)  // 결합된 쿼리
                .build();

        // OpenSearch 클라이언트를 사용하여 검색 요청 실행
        SearchResponse<MemberDocumentReq> response = openSearchClient.search(searchRequest, MemberDocumentReq.class);

        // 검색 결과를 MemberDocumentReq 리스트로 변환하여 반환
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    // 특정 키 존재 여부 확인 메서드
    public Query createTechStackExistsQuery(String techStackKey) {
        // exists 쿼리 작성
        return QueryBuilders.bool()
                .should(QueryBuilders.exists()
                        .field("techStacks." + techStackKey)
                        .build()._toQuery())
                .build()._toQuery();
    }


    public String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return localDateTime.format(formatter);
    }
}