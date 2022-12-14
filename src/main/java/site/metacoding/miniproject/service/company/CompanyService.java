package site.metacoding.miniproject.service.company;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.miniproject.domain.career.Career;
import site.metacoding.miniproject.domain.career.CareerDao;
import site.metacoding.miniproject.domain.category.Category;
import site.metacoding.miniproject.domain.category.CategoryDao;
import site.metacoding.miniproject.domain.company.Company;
import site.metacoding.miniproject.domain.company.CompanyDao;
import site.metacoding.miniproject.domain.jobpostingboard.JobPostingBoard;
import site.metacoding.miniproject.domain.jobpostingboard.JobPostingBoardDao;
import site.metacoding.miniproject.domain.users.Users;
import site.metacoding.miniproject.domain.users.UsersDao;
import site.metacoding.miniproject.dto.company.CompanyReqDto.CompanyUpdateReqDto;
import site.metacoding.miniproject.dto.company.CompanyRespDto.CompanyAddressRespDto;
import site.metacoding.miniproject.dto.company.CompanyRespDto.CompanyDetailRespDto;
import site.metacoding.miniproject.dto.company.CompanyRespDto.CompanyUpdateFormRespDto;
import site.metacoding.miniproject.dto.company.CompanyRespDto.CompanyUpdateRespDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardReqDto.JobPostingBoardInsertReqDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardReqDto.JobPostingBoardUpdateReqDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardRespDto.JobPostingAllRespDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardRespDto.JobPostingBoardAllRespDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardRespDto.JobPostingBoardDetailRespDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardRespDto.JobPostingBoardInsertRespDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardRespDto.JobPostingBoardUpdateRespDto;
import site.metacoding.miniproject.dto.jobpostingboard.JobPostingBoardRespDto.PagingDto;
import site.metacoding.miniproject.exception.ApiException;

@Service
@RequiredArgsConstructor
public class CompanyService {

	private final JobPostingBoardDao jobPostingBoardDao;
	private final CategoryDao categoryDao;
	private final CareerDao careerDao;
	private final CompanyDao companyDao;
	private final UsersDao usersDao;

	@Transactional(readOnly = true)
	public CompanyDetailRespDto findByCompany(Integer companyId) {
		CompanyDetailRespDto companyPS = companyDao.findByCompany(companyId);
		if (companyPS == null) {
			throw new ApiException("?????? ????????? ?????? ??? ????????????.");
		}

		String address = companyPS.getCompanyAddress();
		String[] arry = address.split(",");
		for (int i = 0; i < arry.length; i++) {
			companyPS.setZoneCode(arry[0]);
			companyPS.setRoadJibunAddr(arry[1]);
			companyPS.setDetailAddress(arry[2]);
		}
		CompanyDetailRespDto companyDetailRespDto = new CompanyDetailRespDto(companyPS);
		return companyDetailRespDto;
	}

	// ??? ?????? ???????????? ????????? ????????????
	@Transactional(readOnly = true)
	public CompanyUpdateFormRespDto companyUpdateById(Integer companyId) {
		CompanyUpdateFormRespDto companyUpdateFormRespDto = companyDao.companyUpdateById(companyId);
		if (companyUpdateFormRespDto == null) {
			throw new ApiException("?????? ????????? ?????? ??? ????????????.");
		}
		return companyUpdateFormRespDto;
	}

	// ?????? ?????? ??????
	// ?????? ?????? ????????? ?????? validation ?????????.
	@Transactional(rollbackFor = Exception.class)
	public CompanyUpdateRespDto updateCompany(Integer userId, Integer companyId,
			CompanyUpdateReqDto companyUpdateReqDto) {

		try {
			companyUpdateReqDto.companyUpdateDtoPictureSet();
		} catch (Exception e) {
			throw new ApiException("???????????? ??? ??????");
		}
		// user???????????? ??????
		Users companyUserPS = usersDao.findById(userId);
		if (companyUserPS == null) {
			throw new ApiException("?????? " + userId + " ?????? ???????????? ?????? ??? ????????????.");
		}
		companyUserPS.update(companyUserPS);
		usersDao.update(companyUserPS);

		// personal ???????????? ??????
		Company companyPS = companyDao.findById(companyId);
		if (companyPS == null) {
			throw new ApiException("?????? " + companyId + " ?????? ???????????? ?????? ??? ????????????.");
		}
		companyPS.updateCompany(companyUpdateReqDto);
		companyDao.update(companyPS);
		CompanyUpdateRespDto companyUpdateRespDto = new CompanyUpdateRespDto(companyPS, companyUserPS);

		return companyUpdateRespDto;
	}

	// ???????????? ?????? (category,career,jobPostingboard)
	// ???????????? ????????? ?????? validation ?????????.
	@Transactional(rollbackFor = Exception.class)
	public JobPostingBoardInsertRespDto insertJobPostingBoard(JobPostingBoardInsertReqDto jobPostingBoardInsertReqDto) {
		if (jobPostingBoardInsertReqDto.getCompanyId() == null) {
			throw new ApiException("companyId??? null?????????.");
		}
		Category categoryPS = jobPostingBoardInsertReqDto.JobPostingBoardInsertRespDtoToCategoryEntity();
		categoryDao.insert(categoryPS);

		Career careerPS = jobPostingBoardInsertReqDto.JobPostingBoardInsertRespDtoToCareerEntity();
		careerDao.insert(careerPS);
		// if ??????
		if (!(categoryPS.getCategoryId() != null && careerPS.getCareerId() != null)) {
			throw new ApiException("???????????? ?????? ??????");
		}
		JobPostingBoard jobPostingBoardPS = jobPostingBoardInsertReqDto
				.JobPostingBoardInsertReqDtoJobPostingBoardToEntity();
		jobPostingBoardPS.setJobPostingBoardCategoryId(categoryPS.getCategoryId());
		jobPostingBoardPS.setJobPostingBoardCareerId(careerPS.getCareerId());
		jobPostingBoardDao.insert(jobPostingBoardPS);

		JobPostingBoardInsertRespDto jobPostingBoardInsertRespDto = new JobPostingBoardInsertRespDto(jobPostingBoardPS,
				categoryPS, careerPS);
		return jobPostingBoardInsertRespDto;
	}

	// ???????????? ?????????
	public List<JobPostingBoardAllRespDto> jobPostingBoardList(Integer companyId) {
		List<JobPostingBoardAllRespDto> jobPostingBoardList = jobPostingBoardDao
				.jobPostingBoardList(companyId);
		if (jobPostingBoardList == null) {
			throw new ApiException("??????????????? ????????????.");
		}
		// TimeStamp to String
		for (JobPostingBoardAllRespDto deadLine : jobPostingBoardList) {
			Timestamp ts = deadLine.getJobPostingBoardDeadline();
			Date date = new Date();
			date.setTime(ts.getTime());
			String formattedDate = new SimpleDateFormat("yyyy???MM???dd???").format(date);
			deadLine.setFormatDeadLine(formattedDate);
		}

		return jobPostingBoardList;
	}

	// ???????????? ?????? ??????
	@Transactional(readOnly = true)
	public JobPostingBoardDetailRespDto jobPostingBoardDetail(Integer jobPostingBoardId, Integer companyId) {
		// .. ????????? ?????? ????????????..
		Company companyPS = companyDao.findById(companyId);
		JobPostingBoard jobPostingBoardPS = jobPostingBoardDao.findById(jobPostingBoardId);
		if (jobPostingBoardPS == null) {
			throw new ApiException("?????? " + jobPostingBoardId + " ??? ??????????????? ?????? ??? ????????????.");
		}

		if (companyPS == null || jobPostingBoardPS.getCompanyId() != companyPS.getCompanyId()) {
			throw new ApiException("?????? ???????????? ?????? ????????? ????????????.");
		}
		JobPostingBoardDetailRespDto jobPostingBoardDetailRespDto = jobPostingBoardDao
				.findByJobPostingBoard(jobPostingBoardId);
		Timestamp ts = jobPostingBoardDetailRespDto.getJobPostingBoardDeadline();
		Date date = new Date();
		date.setTime(ts.getTime());
		String formattedDate = new SimpleDateFormat("yyyy???MM???dd???").format(date);
		jobPostingBoardDetailRespDto.setFormatDeadLine(formattedDate);
		return jobPostingBoardDetailRespDto;
	}

	// ???????????? ?????? (jobpostingboard,career,Category)
	@Transactional(rollbackFor = Exception.class)
	public JobPostingBoardUpdateRespDto updateJobPostingBoard(
			JobPostingBoardUpdateReqDto jobPostingBoardUpdateReqDto, Integer jobPostingBoardId) {
		// Integer categoryId,Integer careerId,
		jobPostingBoardUpdateReqDto.setJobPostingBoardId(jobPostingBoardId);
		JobPostingBoard jobPostingBoardPS2Board = jobPostingBoardUpdateReqDto.jobPostingBoardUpdate();

		jobPostingBoardDao.update(jobPostingBoardPS2Board);
		JobPostingBoard jobPostingBoardPS = jobPostingBoardDao.findById(jobPostingBoardId);

		Category categoryPS = categoryDao.findById(jobPostingBoardPS.getJobPostingBoardCategoryId());
		categoryPS = jobPostingBoardUpdateReqDto.jobPostingUpdateReqDtoToCategoryEntity();
		categoryDao.update(categoryPS);

		Career careerPS = careerDao.findById(jobPostingBoardPS.getJobPostingBoardCareerId());
		careerPS.updateCareer(jobPostingBoardUpdateReqDto);
		careerDao.update(careerPS);
		JobPostingBoardUpdateRespDto jobPostingBoardUpdateRespDto = new JobPostingBoardUpdateRespDto(jobPostingBoardPS,
				categoryPS, careerPS);

		return jobPostingBoardUpdateRespDto;

	}

	// ?????? ?????? ??????
	@Transactional(rollbackFor = Exception.class)
	public void deleteJobposting(Integer jobPostingBoardId) {
		JobPostingBoard jobPostingBoard = jobPostingBoardDao.findById(jobPostingBoardId);
		if (jobPostingBoard == null) {
			throw new RuntimeException("??????" + jobPostingBoardId + "??? ???????????? ????????????.");
		}

		jobPostingBoardDao.deleteById(jobPostingBoardId);
	}

	// ?????? ???????????? ?????? ?????? (?????????+??????+????????????id???)
	@Transactional(readOnly = true)
	public List<JobPostingAllRespDto> findAllJobPostingBoard(JobPostingAllRespDto jobPostingAllRespDto) {
		if (jobPostingAllRespDto.getKeyword() == null || jobPostingAllRespDto.getKeyword().isEmpty()) {
			List<JobPostingBoard> jobPostingBoardList = jobPostingBoardDao.findCategory(
					jobPostingAllRespDto.getStartNum(),
					jobPostingAllRespDto.getId());
			List<JobPostingAllRespDto> jobPostingAllRespDtoList = new ArrayList<>();
			for (JobPostingBoard jobPostingBoard : jobPostingBoardList) {
				Timestamp ts = jobPostingBoard.getJobPostingBoardDeadline();
				Date date = new Date();
				date.setTime(ts.getTime());
				String formattedDate = new SimpleDateFormat("yyyy???MM???dd???").format(date);
				jobPostingBoard.setFormatDeadLine(formattedDate);
				jobPostingAllRespDtoList.add(new JobPostingAllRespDto(jobPostingBoard));
			}
			PagingDto paging = jobPostingBoardDao.jobPostingBoardPaging(jobPostingAllRespDto.getPage(),
					jobPostingAllRespDto.getKeyword());
			paging.makeBlockInfo(jobPostingAllRespDto.getKeyword());
			return jobPostingAllRespDtoList;
		} else {
			List<JobPostingBoard> jobPostingBoardList = jobPostingBoardDao.findCategorySearch(
					jobPostingAllRespDto.getStartNum(),
					jobPostingAllRespDto.getKeyword(), jobPostingAllRespDto.getId());
			// TimeStamp to String
			for (JobPostingBoard deadLine : jobPostingBoardList) {
				Timestamp ts = deadLine.getJobPostingBoardDeadline();
				Date date = new Date();
				date.setTime(ts.getTime());
				String formattedDate = new SimpleDateFormat("yyyy???MM???dd???").format(date);
				deadLine.setFormatDeadLine(formattedDate);
			}
			List<JobPostingAllRespDto> jobPostingAllRespDtoList = new ArrayList<>();
			for (JobPostingBoard jobPostingBoard : jobPostingBoardList) {
				jobPostingAllRespDtoList.add(new JobPostingAllRespDto(jobPostingBoard));
			}
			PagingDto paging = jobPostingBoardDao.jobPostingBoardPaging(jobPostingAllRespDto.getPage(),
					jobPostingAllRespDto.getKeyword());
			paging.makeBlockInfo(jobPostingAllRespDto.getKeyword());
			return jobPostingAllRespDtoList;
		}
	}

}
