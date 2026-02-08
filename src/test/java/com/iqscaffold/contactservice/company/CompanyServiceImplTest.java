package com.iqscaffold.contactservice.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.iqscaffold.contactservice.shared.exception.ContactNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

  @Mock
  private CompanyRepository companyRepository;

  @Captor
  private ArgumentCaptor<Company> companyCaptor;

  private CompanyServiceImpl companyService;

  @BeforeEach
  void setUp() {
    companyService = new CompanyServiceImpl(companyRepository);
  }

  @Test
  @DisplayName("Should create company successfully")
  void shouldCreateCompany() {
    // Arrange
    var company = createTestCompany();
    when(companyRepository.save(any(Company.class))).thenReturn(company);

    // Act
    var result = companyService.createCompany(company);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Company");
    verify(companyRepository).save(company);
  }

  @Test
  @DisplayName("Should get company by id")
  void shouldGetCompanyById() {
    // Arrange
    var company = createTestCompany();
    when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

    // Act
    var result = companyService.getCompanyById(1L);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(1L);
    verify(companyRepository).findById(1L);
  }

  @Test
  @DisplayName("Should return empty when company not found")
  void shouldReturnEmptyWhenCompanyNotFound() {
    // Arrange
    when(companyRepository.findById(999L)).thenReturn(Optional.empty());

    // Act
    var result = companyService.getCompanyById(999L);

    // Assert
    assertThat(result).isEmpty();
    verify(companyRepository).findById(999L);
  }

  @Test
  @DisplayName("Should get all companies with pagination")
  void shouldGetAllCompaniesWithPagination() {
    // Arrange
    var companies = List.of(createTestCompany());
    var page = new PageImpl<>(companies);
    var pageable = PageRequest.of(0, 10);
    when(companyRepository.findAll(pageable)).thenReturn(page);

    // Act
    Page<Company> result = companyService.getAllCompanies(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(companyRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should search companies by name")
  void shouldSearchCompaniesByName() {
    // Arrange
    var companies = List.of(createTestCompany());
    var page = new PageImpl<>(companies);
    var pageable = PageRequest.of(0, 10);
    when(companyRepository.findByNameContainingIgnoreCase("Test", pageable)).thenReturn(page);

    // Act
    Page<Company> result = companyService.searchCompanies("Test", pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(companyRepository).findByNameContainingIgnoreCase("Test", pageable);
  }

  @Test
  @DisplayName("Should get companies by industry")
  void shouldGetCompaniesByIndustry() {
    // Arrange
    var companies = List.of(createTestCompany());
    var page = new PageImpl<>(companies);
    var pageable = PageRequest.of(0, 10);
    when(companyRepository.findByIndustry("Technology", pageable)).thenReturn(page);

    // Act
    Page<Company> result = companyService.getCompaniesByIndustry("Technology", pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(companyRepository).findByIndustry("Technology", pageable);
  }

  @Test
  @DisplayName("Should get companies by status")
  void shouldGetCompaniesByStatus() {
    // Arrange
    var companies = List.of(createTestCompany());
    var page = new PageImpl<>(companies);
    var pageable = PageRequest.of(0, 10);
    when(companyRepository.findByStatus(CompanyStatus.ACTIVE, pageable)).thenReturn(page);

    // Act
    Page<Company> result = companyService.getCompaniesByStatus(CompanyStatus.ACTIVE, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    verify(companyRepository).findByStatus(CompanyStatus.ACTIVE, pageable);
  }

  @Test
  @DisplayName("Should get child companies")
  void shouldGetChildCompanies() {
    // Arrange
    var childCompany = createTestCompany();
    childCompany.setId(2L);
    childCompany.setParentCompanyId(1L);
    when(companyRepository.findByParentCompanyId(1L)).thenReturn(List.of(childCompany));

    // Act
    var result = companyService.getChildCompanies(1L);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getParentCompanyId()).isEqualTo(1L);
    verify(companyRepository).findByParentCompanyId(1L);
  }

  @Test
  @DisplayName("Should update company successfully")
  void shouldUpdateCompany() {
    // Arrange
    var company = createTestCompany();
    when(companyRepository.existsById(1L)).thenReturn(true);
    when(companyRepository.save(any(Company.class))).thenReturn(company);

    // Act
    var result = companyService.updateCompany(1L, company);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(companyRepository).existsById(1L);
    verify(companyRepository).save(companyCaptor.capture());
    assertThat(companyCaptor.getValue().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent company")
  void shouldThrowExceptionWhenUpdatingNonExistentCompany() {
    // Arrange
    var company = createTestCompany();
    when(companyRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> companyService.updateCompany(999L, company))
        .isInstanceOf(ContactNotFoundException.class)
        .hasMessageContaining("Company not found");
  }

  @Test
  @DisplayName("Should delete company successfully")
  void shouldDeleteCompany() {
    // Act
    companyService.deleteCompany(1L);

    // Assert
    verify(companyRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should check if company exists by name")
  void shouldCheckIfCompanyExistsByName() {
    // Arrange
    when(companyRepository.existsByNameIgnoreCase("Test Company")).thenReturn(true);

    // Act
    var result = companyService.existsByName("Test Company");

    // Assert
    assertThat(result).isTrue();
    verify(companyRepository).existsByNameIgnoreCase("Test Company");
  }

  @Test
  @DisplayName("Should return false when company does not exist by name")
  void shouldReturnFalseWhenCompanyDoesNotExistByName() {
    // Arrange
    when(companyRepository.existsByNameIgnoreCase("Non-existent")).thenReturn(false);

    // Act
    var result = companyService.existsByName("Non-existent");

    // Assert
    assertThat(result).isFalse();
    verify(companyRepository).existsByNameIgnoreCase("Non-existent");
  }

  private Company createTestCompany() {
    var company = new Company();
    company.setId(1L);
    company.setName("Test Company");
    company.setIndustry("Technology");
    company.setWebsite("https://example.com");
    company.setPhone("+1234567890");
    company.setEmail("info@example.com");
    company.setStatus(CompanyStatus.ACTIVE);
    company.setSize("100-500");
    company.setCreatedBy("test-user");
    company.setUpdatedBy("test-user");
    company.setCreatedAt(LocalDateTime.now());
    company.setUpdatedAt(LocalDateTime.now());
    return company;
  }
}
