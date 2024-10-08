package site.hesil.latteve_spring.domains.search.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * packageName    : site.hesil.latteve_spring.domains.search.service
 * fileName       : SearchIndexingService
 * author         : Heeseon
 * date           : 2024-08-30
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-08-30        Heeseon       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexingService {
//
//    private final OpenSearchClient openSearchClient;
//    private final MemberRepository memberRepository;
//    private final ProjectRepository projectRepository;
//    private final MemberStackRepository memberStackRepository;
//    private final ProjectStackRepository projectStackRepository;
//    private final ProjectMemberRepository projectMemberRepository;
//    private final RecruitmentRepository recruitmentRepository;
//    private final TechStackRepository techStackRepository;
//    private final MemberJobRepository memberJobRepository;
//    private final JobRepository jobRepository;
//    private final ProjectLikeRepository projectLikeRepository;
//
//
//    public void indexProject(Long projectId) throws IOException {
//        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
//
//        // 프로젝트에 연관된 기술 스택  정보 가져옴
//        List<ProjectStack> projectTechStacks = projectStackRepository.findAllByProject_ProjectId(projectId);
//
//        // techStack list로 저장
//        List<ProjectDocReq.TechStack> techStackList= new ArrayList<>();
//        for (ProjectStack projectStack : projectTechStacks) {
//            Long techStackId = projectStack.getTechStack().getTechStackId();
//            if(techStackId == 1){
//                techStackList.add(new ProjectDocReq.TechStack(projectStack.getCustomStack(), null));
//            }else{
//                Optional<TechStack> techStackOpt = techStackRepository.findById(projectStack.getTechStack().getTechStackId());
//                if (techStackOpt.isPresent()) {
//                    TechStack techStack = techStackOpt.get();
//                    String name = techStack.getName();
//                    String imgUrl = techStack.getImgUrl();
//                    techStackList.add(new ProjectDocReq.TechStack(name, imgUrl));
//                }
//            }
//
//        }
//        // 프로젝트에 필요한 인원
//        Integer requiredMemberCount = recruitmentRepository.findMemberCountByProject_ProjectId(projectId);
//
//        ProjectDocReq projectDocument = ProjectDocReq.builder()
//                .projectId(project.getProjectId())
//                .name(project.getName())
//                .imgUrl(project.getImgUrl())
//                .duration(project.getDuration())
//                .projectTechStack(techStackList)
//                .teamCnt(requiredMemberCount)
//                .status(convertStatusToString(project.getStatus()))
//                .createdAt(formatLocalDateTime(project.getCreatedAt()))
//                .build();
//    IndexRequest<ProjectDocReq> indexRequest = new IndexRequest.Builder<ProjectDocReq>()
//            .index("projects")
//            .id(project.getProjectId().toString()) // 동일한 ID를 가진 문서가 있으면 업데이트
//            .document(projectDocument)
//            .build();
//        openSearchClient.index(indexRequest);
//
//        log.info("project 인덱싱");
//    }
//
////    public void indexProjectLike(ProjectLike projectLike) throws IOException {
////        Long projectId = projectLike.getProjectLikeId().getProjectId();
////        Long memberId =projectLike.getProjectLikeId().getMemberId();
////        // 좋아요 수 가져오기
////        ProjectLikeDocReq projectLikeDocReq = ProjectLikeDocReq.builder()
////                .projectId(projectId)
////                .memberId(memberId)
////                .build();
////
////        // Elasticsearch에 인덱싱
////        IndexRequest<ProjectLikeDocReq> indexRequest = new IndexRequest.Builder<ProjectLikeDocReq>()
////                .index("project_likes")
////                .id(projectId + "-" + memberId)
////                .document(projectLikeDocReq)
////                .build();
////        openSearchClient.index(indexRequest);
////
////        log.info("project like 인덱싱");
////    }
////
////    public void deleteProjectLike(ProjectLike projectLike) throws IOException {
////        String docId = projectLike.getProjectLikeId().getProjectId() + "-" + projectLike.getProjectLikeId().getMemberId();
////
////        DeleteRequest deleteRequest = new DeleteRequest.Builder()
////                .index("project_likes")
////                .id(docId)
////                .build();
////
////        openSearchClient.delete(deleteRequest);
////        log.info("ProjectLike 삭제 완료: Project ID = {}, Member ID = {}", projectLike.getProjectLikeId().getProjectId(), projectLike.getProjectLikeId().getMemberId());
////    }
//
//
//    public void indexProjectLike(Long projectId) throws IOException {
//        // 좋아요 수 가져오기
//        ProjectLikeDocReq projectLikeDocReq = ProjectLikeDocReq.builder()
//                .projectId(projectId)
//                .likeCount(projectLikeRepository.countProjectLikeByProject_ProjectId(projectId))
//                .build();
//
//        // Elasticsearch에 인덱싱
//        IndexRequest<ProjectLikeDocReq> indexRequest = new IndexRequest.Builder<ProjectLikeDocReq>()
//                .index("project_likes")
//                .id(projectId.toString())
//                .document(projectLikeDocReq)
//                .build();
//        openSearchClient.index(indexRequest);
//
//        log.info("project like 인덱싱");
//    }
//
//
////    public void indexProjectMember(AcceptedProjectMemberRequest acceptedProjectMemberRequest) throws IOException{
////        Long projectId = acceptedProjectMemberRequest.projectId();
////        Long memberId = acceptedProjectMemberRequest.memberId();
////
////        String docId = projectId + "-" + memberId;
////
////        ProjectMemberDocReq projectMemberDocReq = ProjectMemberDocReq.builder()
////                .projectId(projectId)
////                .memberId(memberId)
////                .jobId(acceptedProjectMemberRequest.jobId())
////                .build();
////
////        IndexRequest<ProjectMemberDocReq> indexRequest = new IndexRequest.Builder<ProjectMemberDocReq>()
////                .index("project_members")
////                .id(docId)
////                .document(projectMemberDocReq)
////                .build();
////        openSearchClient.index(indexRequest);
////
////        log.info("project member 인덱싱");
////    }
//
//    public void indexProjectMember(Long projectId ) throws IOException{
//
//        // 프로젝트에 지원한 인원
//        Integer currentMemberCount = projectMemberRepository.findApprovedMemberCountByProject_ProjectId(projectId);
//
//        ProjectMemberDocReq projectMemberDocReq = ProjectMemberDocReq.builder()
//                .projectId(projectId)
//                .currentMemberCount(currentMemberCount)
//                .build();
//
//        IndexRequest<ProjectMemberDocReq> indexRequest = new IndexRequest.Builder<ProjectMemberDocReq>()
//                .index("project_members")
//                .id(projectId.toString())
//                .document(projectMemberDocReq)
//                .build();
//        openSearchClient.index(indexRequest);
//
//        log.info("project member 인덱싱");
//    }
//    public void indexProjectWith(Long projectId) throws IOException {
//        Project project = projectRepository.findById(projectId)
//                .orElseThrow(() -> new RuntimeException("Project not found"));
//
//        // 프로젝트 기술 스택 리스트 가져옴
//        List<ProjectStack> projectStacks = projectStackRepository.findAllByProject_ProjectId(projectId);
//        List<ProjectSearchResponse.TechStack> techStackList = new ArrayList<>();
//        for (ProjectStack stack : projectStacks) {
//            TechStack techStack = stack.getTechStack();
//            techStackList.add(new ProjectSearchResponse.TechStack(techStack.getName(), techStack.getImgUrl()));
//        }
//
//        // 좋아요 수
//        Long likeCount = projectLikeRepository.countProjectLikeByProject_ProjectId(projectId);
//
//        // 프로젝트에 필요한 인원
//        Integer requiredMemberCount = recruitmentRepository.findMemberCountByProject_ProjectId(project.getProjectId());
//        // 프로젝트에 지원한 인원
//        Integer currentMemberCount = projectMemberRepository.findApprovedMemberCountByProject_ProjectId(project.getProjectId());
//
//        // ProjectDocumentReq 생성
//        ProjectSearchResponse projectDocumentReq = ProjectSearchResponse.builder()
//                .projectId(project.getProjectId())
//                .name(project.getName())
//                .imgUrl(project.getImgUrl())
//                .duration(project.getDuration())
//                .projectTechStack(techStackList)
//                .cntLike(likeCount)
//                .currentCnt(currentMemberCount)
//                .teamCnt(requiredMemberCount)
//                .status(convertStatusToString(project.getStatus()))
//                .createdAt(formatLocalDateTime(project.getCreatedAt()))
//                .build();
//
//        // Elasticsearch에 인덱싱
//        IndexRequest<ProjectSearchResponse> indexRequest = new IndexRequest.Builder<ProjectSearchResponse>()
//                .index("projects")
//                .id(project.getProjectId().toString()) // 동일한 ID를 가진 문서가 있으면 업데이트
//                .document(projectDocumentReq)
//                .build();
//        openSearchClient.index(indexRequest);
//        // OpenSearch에 인덱스 요청 생성
//    }
//
//    @Transactional
//    public void indexProjectsToOpenSearch() throws IOException {
//        List<Project> projects = projectRepository.findAll();
//        //Project -> Index
//        for(Project project : projects) {
//            // 프로젝트에 연관된 기술 스택  정보 가져옴
//            List<ProjectStack> projectTechStacks = projectStackRepository.findAllByProject_ProjectId(project.getProjectId());
//
//            // techStack list로 저장
//            List<ProjectSearchResponse.TechStack> techStackList= new ArrayList<>();
//            for (ProjectStack projectStack : projectTechStacks) {
//                Long techStackId = projectStack.getTechStack().getTechStackId();
//                if(techStackId == 1){
//                   techStackList.add(new ProjectSearchResponse.TechStack(projectStack.getCustomStack(), null));
//                }else{
//                    Optional<TechStack> techStackOpt = techStackRepository.findById(projectStack.getTechStack().getTechStackId());
//                    if (techStackOpt.isPresent()) {
//                        TechStack techStack = techStackOpt.get();
//                        String name = techStack.getName();
//                        String imgUrl = techStack.getImgUrl();
//                        techStackList.add(new ProjectSearchResponse.TechStack(name, imgUrl));
//                    }
//                }
//
//            }
//
//            String statusToString = convertStatusToString(project.getStatus());
//
//
//            // 좋아요 수
//            Long cntLike = projectLikeRepository.countProjectLikeByProject_ProjectId(project.getProjectId());
//
//            // 프로젝트에 필요한 인원
//            Integer requiredMemberCount = recruitmentRepository.findMemberCountByProject_ProjectId(project.getProjectId());
//            // 프로젝트에 지원한 인원
//            Integer currentMemberCount = projectMemberRepository.findApprovedMemberCountByProject_ProjectId(project.getProjectId());
//
//            // ProjectDocumentReq 생성
//            ProjectSearchResponse projectDocumentReq = ProjectSearchResponse.builder()
//                    .projectId(project.getProjectId())
//                    .name(project.getName())
//                    .imgUrl(project.getImgUrl())
//                    .duration(project.getDuration())
//                    .projectTechStack(techStackList)
//                    .teamCnt(requiredMemberCount)
//                    .currentCnt(currentMemberCount)
//                    .cntLike(cntLike)
//                    .status(statusToString)
//                    .createdAt( formatLocalDateTime(project.getCreatedAt()))
//                    .build();
//
//            // Elasticsearch에 인덱싱
//            IndexRequest<ProjectSearchResponse> indexRequest = new IndexRequest.Builder<ProjectSearchResponse>()
//                    .index("projects")
//                    .id(project.getProjectId().toString())
//                    .document(projectDocumentReq)
//                    .build();
//            openSearchClient.index(indexRequest);
//        }
//    }
//    public String formatLocalDateTime(LocalDateTime localDateTime) {
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//        return localDateTime.format(formatter);
//    }
//
//    private String convertStatusToString(int status) {
//        switch (status) {
//            case 0: return "모집중";
//            case 1: return "진행중";
//            case 2: return "종료";
//            default: return "unknown";
//        }
//    }
//
//    @Transactional
//    public void indexMember(Member member) throws IOException {
//
//        // 멤버에 연관된 기술 스택 정보 가져옴
//        List<MemberStack> memberStacks = memberStackRepository.findAllByMember_MemberId(member.getMemberId());
//        // techStack list로 저장
//        List<MemberDocumentReq.TechStack> techStackList = new ArrayList<>();
//        for (MemberStack memberStack : memberStacks) {
//            Long techStackId = memberStack.getTechStack().getTechStackId();
//            if(techStackId == 1){
//                techStackList.add(new MemberDocumentReq.TechStack(memberStack.getCustomStack(), null));
//            }else{
//                Optional<TechStack> techStackOpt = techStackRepository.findById(memberStack.getTechStack().getTechStackId());
//                if (techStackOpt.isPresent()) {
//                    TechStack techStack = techStackOpt.get();
//                    String name = techStack.getName();
//                    String imgUrl = techStack.getImgUrl();
//
//                    // TechStack 객체를 리스트에 추가
//                    techStackList.add(new MemberDocumentReq.TechStack(name, imgUrl));
//                }
//            }
//
//            // 멤버의 직무 정보 가져옴
//            List<MemberJob> memberJobs = memberJobRepository.findAllByMemberAndMemberId(member.getMemberId());
//
//
//            List<String> jobList = new ArrayList<>();
//            // 멤버의 직무 이름 list로 저장
//            for(MemberJob memberJob : memberJobs) {
//                Optional<Job> jobOpt = jobRepository.findById(memberJob.getJob().getJobId());
//                jobOpt.ifPresent(job -> jobList.add(job.getName()));
//
//            }
//
//            // Member가 참여한 프로젝트 개수
//            int ongoingProjectCount = projectRepository.countProjectsByMemberIdAndStatus(member.getMemberId(), 1);
//            int completedProjectCount = projectRepository.countProjectsByMemberIdAndStatus(member.getMemberId(), 2);
//
//            // careerSortValue 계산
//            int careerSortValue = calculateCareerSortValue(Collections.singletonList(member.getCareer()));
//
//            // MemberDocumentReq 생성
//            MemberDocumentReq memberDocumentReq = MemberDocumentReq.builder()
//                    .memberId(member.getMemberId())
//                    .memberNickname(member.getNickname())
//                    .memberImg(member.getImgUrl())
//                    .memberGithub(member.getGithub())
//                    .techStack(techStackList)  // TechStack 리스트 전달
//                    .ongoingProjectCount(ongoingProjectCount)
//                    .completedProjectCount(completedProjectCount)
//                    .memberJob(jobList)
//                    .career(member.getCareer())
//                    .careerSortValue(careerSortValue)
//                    .createdAt(formatLocalDateTime(member.getCreatedAt()))
//                    .build();
//
//            // Elasticsearch에 인덱싱
//            IndexRequest<MemberDocumentReq> indexRequest = new IndexRequest.Builder<MemberDocumentReq>()
//                    .index("members")
//                    .id(member.getMemberId().toString())
//                    .document(memberDocumentReq)
//                    .build();
//            openSearchClient.index(indexRequest);
//        }
//    }
//
//
//    @Transactional
//    public void indexMembersToOpenSearch() throws IOException {
//        List<Member> members = memberRepository.findAll();
//        // Member -> Index
//        for (Member member : members) {
//            // 멤버에 연관된 기술 스택 정보 가져옴
//            List<MemberStack> memberStacks = memberStackRepository.findAllByMember_MemberId(member.getMemberId());
//            // techStack list로 저장
//            List<MemberDocumentReq.TechStack> techStackList = new ArrayList<>();
//            for (MemberStack memberStack : memberStacks) {
//                Long techStackId = memberStack.getTechStack().getTechStackId();
//                if(techStackId == 1){
//                    techStackList.add(new MemberDocumentReq.TechStack(memberStack.getCustomStack(), null));
//                }else{
//                    Optional<TechStack> techStackOpt = techStackRepository.findById(memberStack.getTechStack().getTechStackId());
//                    if (techStackOpt.isPresent()) {
//                        TechStack techStack = techStackOpt.get();
//                        String name = techStack.getName();
//                        String imgUrl = techStack.getImgUrl();
//
//                        // TechStack 객체를 리스트에 추가
//                        techStackList.add(new MemberDocumentReq.TechStack(name, imgUrl));
//                    }
//                }
//
//            }
//            // 멤버의 직무 정보 가져옴
//            List<MemberJob> memberJobs = memberJobRepository.findAllByMemberAndMemberId(member.getMemberId());
//
//
//            List<String> jobList = new ArrayList<>();
//            // 멤버의 직무 이름 list로 저장
//            for(MemberJob memberJob : memberJobs) {
//                Optional<Job> jobOpt = jobRepository.findById(memberJob.getJob().getJobId());
//                jobOpt.ifPresent(job -> jobList.add(job.getName()));
//
//            }
//
//            // Member가 참여한 프로젝트 개수
//            int ongoingProjectCount = projectRepository.countProjectsByMemberIdAndStatus(member.getMemberId(), 1);
//            int completedProjectCount = projectRepository.countProjectsByMemberIdAndStatus(member.getMemberId(), 2);
//
//            // careerSortValue 계산
//            int careerSortValue = calculateCareerSortValue(Collections.singletonList(member.getCareer()));
//
//            // MemberDocumentReq 생성
//            MemberDocumentReq memberDocumentReq = MemberDocumentReq.builder()
//                    .memberId(member.getMemberId())
//                    .memberNickname(member.getNickname())
//                    .memberImg(member.getImgUrl())
//                    .memberGithub(member.getGithub())
//                    .techStack(techStackList)  // TechStack 리스트 전달
//                    .ongoingProjectCount(ongoingProjectCount)
//                    .completedProjectCount(completedProjectCount)
//                    .memberJob(jobList)
//                    .career(member.getCareer())
//                    .careerSortValue(careerSortValue)
//                    .createdAt(formatLocalDateTime(member.getCreatedAt()))
//                    .build();
//
//            // Elasticsearch에 인덱싱
//            IndexRequest<MemberDocumentReq> indexRequest = new IndexRequest.Builder<MemberDocumentReq>()
//                    .index("members")
//                    .id(member.getMemberId().toString())
//                    .document(memberDocumentReq)
//                    .build();
//            openSearchClient.index(indexRequest);
//        }
//    }
//
//    private int calculateCareerSortValue(List<String> career) {
//        // 경력 직무에 따라 정수 값 설정 (예시: 시니어 3, 주니어 2, 신입 1)
//        if (career.contains("시니어")) {
//            return 3;
//        } else if (career.contains("주니어")) {
//            return 2;
//        } else if (career.contains("신입")) {
//            return 1;
//        }
//        return 0; // 기타 또는 알 수 없는 경우
//    }
//
//    public void deleteAllIndex() throws IOException {
//        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest.Builder()
//                .index("projects")  // 삭제할 인덱스 이름
//                .build();
//        openSearchClient.indices().delete(deleteIndexRequest);
//    }
//
//    // 프로젝트 전체 인덱싱
//    public void reindexAllProjects() throws IOException {
//        // 프로젝트 전체 조회
//        List<Project> projects = projectRepository.findAll();
//
//        for (Project project : projects) {
//            indexProject(project.getProjectId());
//        }
//
//        log.info("모든 프로젝트 인덱싱 완료");
//    }
//
//    // 좋아요 전체 인덱싱
//    public void reindexAllProjectLikes() throws IOException {
//        // 모든 좋아요 조회
//        List<ProjectLike> projectLikes = projectLikeRepository.findAll();
//
//        for (ProjectLike projectLike : projectLikes) {
//            indexProjectLike(projectLike.getProjectLikeId().getProjectId());
//        }
//
//        log.info("모든 프로젝트 좋아요 인덱싱 완료");
//    }
//
//    // 프로젝트 멤버 전체 인덱싱
//    public void reindexAllProjectMembers() throws IOException {
//        // 모든 프로젝트 멤버 조회
//        List<ProjectJob> projectJobs = projectMemberRepository.findAll();
//
//        for (ProjectJob projectJob : projectJobs) {
////            AcceptedProjectMemberRequest request = new AcceptedProjectMemberRequest(
////                    projectMember.getProject().getProjectId(),
////                    projectMember.getMember().getMemberId(),
////                    projectMember.getJob().getJobId()
////            );
////            indexProjectMember(request);
//            indexProjectMember(projectJob.getProjectMemberId().getProjectId());
//        }
//
//        log.info("모든 프로젝트 멤버 인덱싱 완료");
//    }
//
//    // 전체 데이터 인덱싱 초기화
//    public void reindexAllData() throws IOException {
//        // 모든 프로젝트 인덱싱
//        reindexAllProjects();
//
//        // 모든 좋아요 인덱싱
//        reindexAllProjectLikes();
//
//        // 모든 프로젝트 멤버 인덱싱
//        reindexAllProjectMembers();
//
//        log.info("모든 프로젝트 관련 데이터 인덱싱 초기화 완료");
//    }

}
