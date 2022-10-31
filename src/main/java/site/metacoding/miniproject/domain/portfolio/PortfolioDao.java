package site.metacoding.miniproject.domain.portfolio;

import java.util.List;

import site.metacoding.miniproject.dto.resume.ResumeRespDto.ResumesDetailDto;

public interface PortfolioDao {

	public void insert(Portfolio portfolio);

	public ResumesDetailDto resumesById(Integer portfolioId);

	public Portfolio findById(Integer portfolioId);

	public void update(Portfolio portfolio);

	public List<Portfolio> findAll();

	public void deleteById(Integer portfolioId);
}
