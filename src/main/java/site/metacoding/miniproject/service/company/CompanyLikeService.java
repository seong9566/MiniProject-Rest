package site.metacoding.miniproject.service.company;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.miniproject.domain.alarm.Alarm;
import site.metacoding.miniproject.domain.alarm.AlarmDao;
import site.metacoding.miniproject.domain.like.companylike.CompanyLike;
import site.metacoding.miniproject.domain.like.companylike.CompanyLikesDao;
import site.metacoding.miniproject.domain.personal.Personal;
import site.metacoding.miniproject.domain.personal.PersonalDao;
import site.metacoding.miniproject.domain.users.Users;
import site.metacoding.miniproject.domain.users.UsersDao;
import site.metacoding.miniproject.dto.like.LikeReqDto.CompanyLikeReqDto;
import site.metacoding.miniproject.dto.like.LikeRespDto.CompanyLikeRespDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignPersonalDto;
import site.metacoding.miniproject.dto.user.UserRespDto.SignedDto;
import site.metacoding.miniproject.utill.AlarmEnum;

@RequiredArgsConstructor
@Service
public class CompanyLikeService {
	private final HttpSession session;
	private final CompanyLikesDao companyLikesDao;
	private final UsersDao usersDao;
	private final AlarmDao alarmDao;
	private final PersonalDao personalDao;

	@Transactional(rollbackFor = RuntimeException.class)
	public CompanyLikeRespDto 좋아요(Integer companyId, CompanyLikeReqDto companyLikeReqDto) {
		HashMap<String, Integer> companylikes = new HashMap<>();

		CompanyLike companyLikePS = companyLikeReqDto.companyLikeEntity();
		companyLikesDao.insert(companyLikePS);

		companylikes.put(AlarmEnum.ALARMCOMPANYLIKEID.key(), companyLikePS.getCompanyLikeId());

		Users users = usersDao.findByCompanyId(companyId);
		Personal personalPS = personalDao.findById(companyLikePS.getPersonalId());
		Alarm alarm = new Alarm(users.getUsersId(), companylikes, personalPS.getPersonalName());

		alarmDao.insert(alarm);
		companyLikePS.setAlarmId(alarm.getAlarmId());
		companyLikesDao.update(companyLikePS);
		CompanyLikeRespDto companyLikeRespDto = new CompanyLikeRespDto(companyLikePS);
		return companyLikeRespDto;

	}

	public CompanyLikeRespDto 좋아요취소(Integer companyId, CompanyLikeReqDto companyLikeReqDto) {
		CompanyLike companyLikePS = companyLikeReqDto.companyLikeEntity();
		companyLikesDao.deleteById(companyLikePS);
		CompanyLikeRespDto companyLikeRespDto = new CompanyLikeRespDto(companyLikePS);
		return companyLikeRespDto;
	}

	public CompanyLike 좋아요확인(Integer personalId, Integer companyId, CompanyLikeReqDto companyLikeReqDto) {
		CompanyLike companyLike = companyLikesDao.findById(companyLikeReqDto);
		return companyLike;
	}
}
