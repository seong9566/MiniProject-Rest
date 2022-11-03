package site.metacoding.miniproject.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import site.metacoding.miniproject.dto.resumes.ResumesReqDto.ResumesInsertReqDto;
import site.metacoding.miniproject.dto.resumes.ResumesReqDto.ResumesUpdateReqDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesAllRespDto;
import site.metacoding.miniproject.dto.resumes.ResumesRespDto.ResumesUpdateRespDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignPersonalDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignedDto;
import site.metacoding.miniproject.service.company.CompanyService;
import site.metacoding.miniproject.service.personal.PersonalLikeService;
import site.metacoding.miniproject.service.personal.PersonalService;
import site.metacoding.miniproject.utill.ValidationCheckUtil;
import site.metacoding.miniproject.web.dto.request.personal.PersonalUpdateDto;
import site.metacoding.miniproject.web.dto.response.ResponseDto;
import site.metacoding.miniproject.web.dto.response.company.CompanyAddressDto;
import site.metacoding.miniproject.web.dto.response.company.CompanyInfoDto;
import site.metacoding.miniproject.web.dto.response.company.CompanyMainDto;
import site.metacoding.miniproject.web.dto.response.etc.PagingDto;
import site.metacoding.miniproject.web.dto.response.jobpostingboard.JobPostingBoardDetailDto;
import site.metacoding.miniproject.web.dto.response.personal.PersonalAddressDto;
import site.metacoding.miniproject.web.dto.response.personal.PersonalMainDto;

@RequiredArgsConstructor
@RestController
public class PersonalController {

	private final HttpSession session;
	private final PersonalService personalService;
	private final CompanyService companyService;
	private final PersonalLikeService personalLikeService;

	// 이력서 작성 하기
	@PostMapping(value = "/s/resumes/insert")
	public ResponseDto<?> insertResumes(@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestPart("reqDto") ResumesInsertReqDto resumesInsertReqDto) throws Exception {
		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		SignPersonalDto signPersonalDto = (SignPersonalDto) principal.getUserInfo();
		resumesInsertReqDto.setPersonalId(signPersonalDto.getPersonalId());
		resumesInsertReqDto.setFile(file);
		return new ResponseDto<>(1, "이력서 등록 성공", personalService.insertResumes(resumesInsertReqDto));
	}

	// 내가 작성한 이력서 목록 보기
	@GetMapping("/resumes/myList")
	public ResponseDto<?> findAllMyResumes(ResumesAllRespDto resumesAllRespDto) {
		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		SignPersonalDto signPersonalDto = (SignPersonalDto) principal.getUserInfo();
		resumesAllRespDto.setPersonalId(signPersonalDto.getPersonalId());
		return new ResponseDto<>(1, "내 이력서 목록 보기 성공", personalService.findAllMyResumes(resumesAllRespDto));
	}

	// 이력서 상세 보기
	@GetMapping("/resumes/{resumesId}")
	public ResponseDto<?> findByResumesId(@PathVariable Integer resumesId) {
		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		// PersonalLike personalLike = personalLikeService.좋아요확인(resumesId,
		// signedDto.getCompanyId());
		// model.addAttribute("personalLike", personalLike);
		return new ResponseDto<>(1, "내 이력서 목록 보기 성공", personalService.findByResumesId(resumesId));
	}

	// 이력서 수정
	// @GetMapping("/personal/resumes/update/{resumesId}")
	// public String updateForm(@PathVariable Integer resumesId) {
	// ResumesDetailDto resumesDetailDtoPS = personalService.resumesById(resumesId);
	// return "personal/resumesUpdateForm";
	// }

	@PutMapping(value = "/s/resumes/update/{resumesId}")
	public ResponseDto<?> updateResumes(@PathVariable Integer resumesId,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestPart("resumesUpdateReqDto") ResumesUpdateReqDto resumesUpdateReqDto)
			throws Exception {
		resumesUpdateReqDto.setFile(file);
		resumesUpdateReqDto.setResumesId(resumesId);
		ResumesUpdateRespDto resumesUpdateRespDto = personalService.updateResumes(resumesUpdateReqDto);
		return new ResponseDto<>(1, "이력서 수정 성공", resumesUpdateRespDto);
	}

	// 이력서 삭제 하기
	@DeleteMapping("/personal/resumes/delete/{resumesId}")
	public @ResponseBody ResponseDto<?> deleteResumes(@PathVariable Integer resumesId) {
		personalService.deleteResumes(resumesId);
		return new ResponseDto<>(1, "이력서 삭제 성공", null);
	}

	// 메인 - 채용공고 or 이력서 리스트 (페이징+검색)
	@GetMapping({ "/", "/main" })
	public String jobPostingBoardList(Model model, Integer page, String keyword) {

		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");

		if (page == null)
			page = 0;
		int startNum = page * 5;

		if (session.getAttribute("principal") == null) {
			if (keyword == null || keyword.isEmpty()) {
				List<PersonalMainDto> jobPostingBoardList = companyService.findAll(startNum);
				PagingDto paging = companyService.jobPostingBoardPaging(page, null);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			} else {
				List<PersonalMainDto> jobPostingBoardList = companyService.findSearch(startNum, keyword);
				PagingDto paging = companyService.jobPostingBoardPaging(page, keyword);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			}
		} else if (principal.getPersonalId() != null) {
			if (keyword == null || keyword.isEmpty()) {
				List<PersonalMainDto> jobPostingBoardList = companyService.findAll(startNum);
				PagingDto paging = companyService.jobPostingBoardPaging(page, null);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			} else {
				List<PersonalMainDto> jobPostingBoardList = companyService.findSearch(startNum, keyword);
				PagingDto paging = companyService.jobPostingBoardPaging(page, keyword);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			}

		} else if (principal.getCompanyId() != null) {
			if (keyword == null || keyword.isEmpty()) {
				List<CompanyMainDto> resumesList = personalService.resumesAll(startNum);
				PagingDto paging = personalService.resumesPaging(page, null);
				paging.makeBlockInfo(keyword);
				model.addAttribute("resumesList", resumesList);
				model.addAttribute("paging", paging);

			} else {
				List<CompanyMainDto> resumesList = personalService.findSearch(startNum, keyword);
				PagingDto paging = personalService.resumesPaging(page, keyword);
				paging.makeBlockInfo(keyword);
				model.addAttribute("resumesList", resumesList);
				model.addAttribute("paging", paging);
			}
		}
		return "personal/main";
	}

	// 메인 - 카테고리별 리스트 보기
	@GetMapping("/main/{id}")
	public String listByCategoryTest(@PathVariable Integer id, Model model, Integer page, String keyword) {

		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");

		if (page == null)
			page = 0;
		int startNum = page * 5;

		if (session.getAttribute("principal") == null) {
			if (keyword == null || keyword.isEmpty()) {
				List<PersonalMainDto> jobPostingBoardList = companyService.findCategory(startNum, id);
				PagingDto paging = companyService.jobPostingBoardPaging(page, null);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			} else {
				List<PersonalMainDto> jobPostingBoardList = companyService.findCategorySearch(startNum, keyword, id);
				PagingDto paging = companyService.jobPostingBoardPaging(page, keyword);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			}
		} else if (principal.getPersonalId() != null) {
			if (keyword == null || keyword.isEmpty()) {
				List<PersonalMainDto> jobPostingBoardList = companyService.findCategory(startNum, id);
				PagingDto paging = companyService.jobPostingBoardPaging(page, null);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			} else {
				List<PersonalMainDto> jobPostingBoardList = companyService.findCategorySearch(startNum, keyword, id);
				PagingDto paging = companyService.jobPostingBoardPaging(page, keyword);
				paging.makeBlockInfo(keyword);
				model.addAttribute("jobPostingBoardList", jobPostingBoardList);
				model.addAttribute("paging", paging);
			}
		} else if (principal.getCompanyId() != null) {
			if (keyword == null || keyword.isEmpty()) {
				List<CompanyMainDto> resumesList = personalService.findCategory(startNum, id);
				PagingDto paging = personalService.resumesPaging(page, null);
				paging.makeBlockInfo(keyword);
				model.addAttribute("resumesList", resumesList);
				model.addAttribute("paging", paging);
			} else {
				List<CompanyMainDto> resumesList = personalService.findCategorySearch(startNum, keyword, id);
				PagingDto paging = personalService.resumesPaging(page, keyword);
				paging.makeBlockInfo(keyword);
				model.addAttribute("resumesList", resumesList);
				model.addAttribute("paging", paging);
			}
		}
		model.addAttribute("number", id);
		return "personal/main";
	}

	// 내정보 보기
	@GetMapping("/api/personal/inform")
	public ResponseDto<?> perosnalDetail() {
		SignedDto<SignPersonalDto> principal = (SignedDto<SignPersonalDto>) session.getAttribute("principal");
		return new ResponseDto<>(1, "성공", personalService.findByPersonal(principal.getUserInfo().getPersonalId()));
		// principal.getUserinfo().getPersonalId()

	}

	// 내정보 수정 보기
	@GetMapping("/api/personal/inform/informUpdate")
	public ResponseDto<?> personalupdate() {
		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		PersonalUpdateDto personalUpdateFormPS = personalService.personalUpdateById(principal.getPersonalId());
		PersonalAddressDto personalAddressPS = personalService.personalAddress(principal.getPersonalId());
		return new ResponseDto<>(1, "성공", null);
	}

	@PutMapping("/personal/personalUpdate")
	public @ResponseBody ResponseDto<?> personalUpdate(@RequestBody PersonalUpdateDto personalUpdateDto) {
		ValidationCheckUtil.valCheckToUpdatePersonal(personalUpdateDto);
		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		personalService.updatePersonal(principal.getUsersId(), principal.getPersonalId(), personalUpdateDto);
		return new ResponseDto<>(1, "수정 성공", null);
	}

	// 채용공고 상세 보기 (개인)
	@GetMapping("/personal/jobPostingBoard/{jobPostingBoardId}")
	public String jobPostingDetailForm(Model model, @PathVariable Integer jobPostingBoardId) {
		JobPostingBoardDetailDto jobPostingPS = companyService.jobPostingOne(jobPostingBoardId);
		// SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		CompanyAddressDto addressPS = companyService.findByAddress(jobPostingPS.getCompanyId());
		model.addAttribute("address", addressPS);
		model.addAttribute("jobPostingPS", jobPostingPS);
		System.out.println("jobpostingLike : " + jobPostingPS.getCompanyPhoneNumber());
		return "personal/jobPostingViewApply";
	}

	// 회사 정보보러 가기(개인)
	@GetMapping("/personal/companyInform/{companyId}")
	public String companyDetailform(Model model, @PathVariable Integer companyId) {
		CompanyInfoDto companyPS = companyService.findCompanyInfo(companyId);
		CompanyAddressDto addressPS = companyService.findByAddress(companyId);
		model.addAttribute("address", addressPS);
		model.addAttribute("companyInfo", companyPS);
		System.out.println("companyPS : " + companyPS.getCount());
		return "personal/companyInform";
	}

}
