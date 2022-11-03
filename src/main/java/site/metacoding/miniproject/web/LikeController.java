package site.metacoding.miniproject.web;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.metacoding.miniproject.dto.like.LikeReqDto.CompanyLikeReqDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignPersonalDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignedDto;
import site.metacoding.miniproject.service.company.CompanyLikeService;
import site.metacoding.miniproject.service.personal.PersonalLikeService;
import site.metacoding.miniproject.web.dto.request.personal.PersonalLikeDto;
import site.metacoding.miniproject.web.dto.response.ResponseDto;

@RequiredArgsConstructor
@RestController
public class LikeController {
	private final HttpSession session;
	private final PersonalLikeService personalLikeService;
	private final CompanyLikeService companyLikeService;

	@PostMapping("/personalLike/{resumesId}/likes")
	public @ResponseBody ResponseDto<?> insertLike(@PathVariable Integer resumesId) {
		// Company company = (Company) session.getAttribute("principal");
		System.out.println(resumesId);
		SignedDto<?> signUserDto = (SignedDto<?>) session.getAttribute("principal");

		personalLikeService.좋아요(resumesId, signUserDto);
		return new ResponseDto<>(1, "좋아요성공", null);

	}

	@DeleteMapping("/personalLike/{resumesId}/likes")
	public @ResponseBody ResponseDto<?> deleteLike(@PathVariable Integer resumesId) {
		SignedDto<?> signUserDto = (SignedDto<?>) session.getAttribute("principal");
		// personalLikeService.좋아요취소(resumesId, signedDto.getCompanyId());
		return new ResponseDto<>(1, "좋아요취소", null);
	}

	@GetMapping("/recommendList")
	public String recommend(Model model) {
		Integer companyId = (Integer) session.getAttribute("companyId");
		List<PersonalLikeDto> personalLikeDto = personalLikeService.좋아요이력서(companyId);
		model.addAttribute("personalLikeList", personalLikeDto);
		return "/company/recommendList";
	}

	@PostMapping("/s/api/companyLike/{companyId}")
	public ResponseDto<?> insertCompanyLike(@PathVariable Integer companyId,
			CompanyLikeReqDto companyLikeReqDto) {
		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		SignPersonalDto signPersonalDto = (SignPersonalDto) principal.getUserInfo();
		companyLikeReqDto.setPersonalId(signPersonalDto.getPersonalId());
		return new ResponseDto<>(1, "좋아요 성공", companyLikeService.좋아요(companyId, companyLikeReqDto));

	}

	@DeleteMapping("/s/api/companyLike/{companyId}")
	public @ResponseBody ResponseDto<?> deleteCompanyLike(@PathVariable Integer companyId,
			CompanyLikeReqDto companyLikeReqDto) {
		SignedDto<?> principal = (SignedDto<?>) session.getAttribute("principal");
		SignPersonalDto signPersonalDto = (SignPersonalDto) principal.getUserInfo();
		companyLikeReqDto.setPersonalId(signPersonalDto.getPersonalId());
		companyLikeService.좋아요취소(companyId, companyLikeReqDto);
		return new ResponseDto<>(1, "좋아요취소", null);
	}

	@GetMapping("/jobPostingViewApply/{companyId}")
	public String company(@PathVariable Integer companyId, Model model) {

		SignedDto<?> signUserDto = (SignedDto<?>) session.getAttribute("principal");

		// CompanyLike companyLike = companyLikeService.좋아요확인(companyId,
		// signedDto.getPersonalId());

		// model.addAttribute("companyLike", companyLike);

		return "/personal/jobPostingViewApply";
	}

}