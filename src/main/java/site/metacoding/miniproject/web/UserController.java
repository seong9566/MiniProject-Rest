package site.metacoding.miniproject.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import site.metacoding.miniproject.config.SessionConfig;
import site.metacoding.miniproject.domain.alarm.Alarm;
import site.metacoding.miniproject.domain.subscribe.Subscribe;
import site.metacoding.miniproject.dto.company.CompanyReqDto.CompanyJoinDto;
import site.metacoding.miniproject.service.users.UsersService;
import site.metacoding.miniproject.utill.ValidationCheckUtil;
import site.metacoding.miniproject.web.dto.request.etc.LoginDto;
import site.metacoding.miniproject.web.dto.request.personal.PersonalJoinDto;
import site.metacoding.miniproject.web.dto.response.ResponseDto;
import site.metacoding.miniproject.web.dto.response.etc.SignedDto;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UsersService userService;
	private final HttpSession session;

	@GetMapping("/loginForm")
	public ResponseDto<?> loginForm() {
		ResponseDto<?> responseDto;
		if (session.getAttribute("principal") == null) {
			responseDto = new ResponseDto<>(-1, "이미 로그인 되어 있음", null);
		}else{
			responseDto = new ResponseDto<>(-1, "성공", null);
		}
		return responseDto;
	}

	@GetMapping("/logout")
	public ResponseDto<?> logout() {
		SessionConfig.logout(session.getId());
		session.removeAttribute("principal");
		session.removeAttribute("companyId");
		session.removeAttribute("personalId");
		session.removeAttribute("subscribe");
		return new ResponseDto<>(-1, "성공", null);
	}

	@GetMapping("/company/joinForm")
	public ResponseDto<?> CompanyJoinForm() {
		return new ResponseDto<>(-1, "성공", null);
	}

	@GetMapping("/personal/joinForm")
	public ResponseDto<?> PersonalJoinForm() {
		return new ResponseDto<>(-1, "성공", null);
	}

	@GetMapping("/company/companyinform")
	public ResponseDto<?> companyInform() {
		return new ResponseDto<>(-1, "성공", null);
	}

	@GetMapping("/checkId/{loginId}")
	public ResponseDto<?> userIdSameCheck(@PathVariable String loginId) {

		ResponseDto<?> responseDto;

		if (loginId == null || loginId == "") {
			responseDto = new ResponseDto<>(-1, "아이디를 입력하여 주세요", null);
			return responseDto;
		}
		Integer userCheck = userService.checkUserId(loginId);
		if (userCheck == null) {
			responseDto = new ResponseDto<>(1, "아이디 중복 없음 사용하셔도 좋습니다.", null);
		} else {
			responseDto = new ResponseDto<>(-1, "아이디 중복이 확인됨", null);
		}
		return responseDto;
	}

	@PostMapping("/login")
	public ResponseDto<?> login(@RequestBody LoginDto loginDto) {

		SignedDto<?> signedDto = userService.login(loginDto);
		List<Subscribe> subscribes = null;

		// if (signedDto == null)
		// 	return new ResponseDto<>(-1, "비밀번호 또는 아이디를 확인하여 주세요", null);

		// if (SessionConfig.getSessionidCheck(signedDto.getUsersId()) != null) {
		// 	return new ResponseDto<>(-2, "중복 로그인 확인됨", null);
		// }

		// session.setAttribute("principal", signedDto);
		// SessionConfig.login(session.getId(), signedDto.getUsersId());

		// if (signedDto.getCompanyId() != null) {
		// 	session.setAttribute("companyId", signedDto.getCompanyId());
		// } else {
		// 	subscribes = userService.findSubscribeinfoByPersonalId(signedDto.getPersonalId());
		// 	session.setAttribute("personalId", signedDto.getPersonalId());
		// 	session.setAttribute("subscribe", subscribes);
		// }

		return new ResponseDto<>(1, "로그인완료", subscribes);
	}

	@PostMapping("/join/personal")
	public ResponseDto<?> joinPersonal(@RequestBody PersonalJoinDto joinDto) {

		ValidationCheckUtil.valCheckToJoinPersonal(joinDto);

		userService.joinPersonal(joinDto);

		LoginDto loginDto = new LoginDto(joinDto);
		SignedDto<?> signedDto = userService.login(loginDto);

		session.setAttribute("principal", signedDto);

		// if (signedDto.getCompanyId() != null) {
		// 	session.setAttribute("companyId", signedDto.getCompanyId());
		// } else {
		// 	session.setAttribute("personalId", signedDto.getPersonalId());
		// }

		return new ResponseDto<>(1, "계정생성완료", null);
	}

	@PostMapping(value = "/join/company")
	public ResponseDto<?> joinCompany(@RequestPart("file") MultipartFile file,
			@RequestPart("joinDto") CompanyJoinDto joinDto) throws Exception {
		
		joinDto.companyJoinDtoPictureSet(file);
		userService.joinCompany(joinDto);

		LoginDto loginDto = new LoginDto(joinDto);
		
		SignedDto<?> signedDto = userService.login(loginDto);

		session.setAttribute("principal", signedDto);

		return new ResponseDto<>(1, "계정생성완료", null);
	}

	@GetMapping("/user/alarm/{usersId}")
	public ResponseDto<?> refreshUserAlarm(@PathVariable Integer usersId) {
		ResponseDto<?> responseDto = null;
		List<Alarm> usersAlarm = userService.userAlarm(usersId);
		if (!usersAlarm.isEmpty())
			responseDto = new ResponseDto<>(1, "통신 성공", usersAlarm);
		return responseDto;
	}

	@DeleteMapping("/user/alarm/{alarmId}")
	public ResponseDto<?> deleteUserAlarm(@PathVariable Integer alarmId) {
		userService.deleteAlarm(alarmId);
		return new ResponseDto<>(1, "삭제 성공", null);
	}

}