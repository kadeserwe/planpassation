package sn.ssi.sigmap.web.rest;

import sn.ssi.sigmap.PlanpassationmsApp;
import sn.ssi.sigmap.config.TestSecurityConfiguration;
import sn.ssi.sigmap.domain.PlanPassation;
import sn.ssi.sigmap.repository.PlanPassationRepository;
import sn.ssi.sigmap.service.PlanPassationService;
import sn.ssi.sigmap.service.dto.PlanPassationDTO;
import sn.ssi.sigmap.service.mapper.PlanPassationMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link PlanPassationResource} REST controller.
 */
@SpringBootTest(classes = { PlanpassationmsApp.class, TestSecurityConfiguration.class })
@AutoConfigureMockMvc
@WithMockUser
public class PlanPassationResourceIT {

    private static final LocalDate DEFAULT_DATE_DEBUT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_DEBUT = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_DATE_FIN = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_FIN = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_COMMENTAIRE = "AAAAAAAAAA";
    private static final String UPDATED_COMMENTAIRE = "BBBBBBBBBB";

    private static final Integer DEFAULT_ANNEE = 1;
    private static final Integer UPDATED_ANNEE = 2;

    @Autowired
    private PlanPassationRepository planPassationRepository;

    @Autowired
    private PlanPassationMapper planPassationMapper;

    @Autowired
    private PlanPassationService planPassationService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPlanPassationMockMvc;

    private PlanPassation planPassation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PlanPassation createEntity(EntityManager em) {
        PlanPassation planPassation = new PlanPassation()
            .dateDebut(DEFAULT_DATE_DEBUT)
            .dateFin(DEFAULT_DATE_FIN)
            .commentaire(DEFAULT_COMMENTAIRE)
            .annee(DEFAULT_ANNEE);
        return planPassation;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PlanPassation createUpdatedEntity(EntityManager em) {
        PlanPassation planPassation = new PlanPassation()
            .dateDebut(UPDATED_DATE_DEBUT)
            .dateFin(UPDATED_DATE_FIN)
            .commentaire(UPDATED_COMMENTAIRE)
            .annee(UPDATED_ANNEE);
        return planPassation;
    }

    @BeforeEach
    public void initTest() {
        planPassation = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlanPassation() throws Exception {
        int databaseSizeBeforeCreate = planPassationRepository.findAll().size();
        // Create the PlanPassation
        PlanPassationDTO planPassationDTO = planPassationMapper.toDto(planPassation);
        restPlanPassationMockMvc.perform(post("/api/plan-passations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planPassationDTO)))
            .andExpect(status().isCreated());

        // Validate the PlanPassation in the database
        List<PlanPassation> planPassationList = planPassationRepository.findAll();
        assertThat(planPassationList).hasSize(databaseSizeBeforeCreate + 1);
        PlanPassation testPlanPassation = planPassationList.get(planPassationList.size() - 1);
        assertThat(testPlanPassation.getDateDebut()).isEqualTo(DEFAULT_DATE_DEBUT);
        assertThat(testPlanPassation.getDateFin()).isEqualTo(DEFAULT_DATE_FIN);
        assertThat(testPlanPassation.getCommentaire()).isEqualTo(DEFAULT_COMMENTAIRE);
        assertThat(testPlanPassation.getAnnee()).isEqualTo(DEFAULT_ANNEE);
    }

    @Test
    @Transactional
    public void createPlanPassationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = planPassationRepository.findAll().size();

        // Create the PlanPassation with an existing ID
        planPassation.setId(1L);
        PlanPassationDTO planPassationDTO = planPassationMapper.toDto(planPassation);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlanPassationMockMvc.perform(post("/api/plan-passations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planPassationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PlanPassation in the database
        List<PlanPassation> planPassationList = planPassationRepository.findAll();
        assertThat(planPassationList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkAnneeIsRequired() throws Exception {
        int databaseSizeBeforeTest = planPassationRepository.findAll().size();
        // set the field null
        planPassation.setAnnee(null);

        // Create the PlanPassation, which fails.
        PlanPassationDTO planPassationDTO = planPassationMapper.toDto(planPassation);


        restPlanPassationMockMvc.perform(post("/api/plan-passations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planPassationDTO)))
            .andExpect(status().isBadRequest());

        List<PlanPassation> planPassationList = planPassationRepository.findAll();
        assertThat(planPassationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlanPassations() throws Exception {
        // Initialize the database
        planPassationRepository.saveAndFlush(planPassation);

        // Get all the planPassationList
        restPlanPassationMockMvc.perform(get("/api/plan-passations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(planPassation.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateDebut").value(hasItem(DEFAULT_DATE_DEBUT.toString())))
            .andExpect(jsonPath("$.[*].dateFin").value(hasItem(DEFAULT_DATE_FIN.toString())))
            .andExpect(jsonPath("$.[*].commentaire").value(hasItem(DEFAULT_COMMENTAIRE)))
            .andExpect(jsonPath("$.[*].annee").value(hasItem(DEFAULT_ANNEE)));
    }
    
    @Test
    @Transactional
    public void getPlanPassation() throws Exception {
        // Initialize the database
        planPassationRepository.saveAndFlush(planPassation);

        // Get the planPassation
        restPlanPassationMockMvc.perform(get("/api/plan-passations/{id}", planPassation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(planPassation.getId().intValue()))
            .andExpect(jsonPath("$.dateDebut").value(DEFAULT_DATE_DEBUT.toString()))
            .andExpect(jsonPath("$.dateFin").value(DEFAULT_DATE_FIN.toString()))
            .andExpect(jsonPath("$.commentaire").value(DEFAULT_COMMENTAIRE))
            .andExpect(jsonPath("$.annee").value(DEFAULT_ANNEE));
    }
    @Test
    @Transactional
    public void getNonExistingPlanPassation() throws Exception {
        // Get the planPassation
        restPlanPassationMockMvc.perform(get("/api/plan-passations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlanPassation() throws Exception {
        // Initialize the database
        planPassationRepository.saveAndFlush(planPassation);

        int databaseSizeBeforeUpdate = planPassationRepository.findAll().size();

        // Update the planPassation
        PlanPassation updatedPlanPassation = planPassationRepository.findById(planPassation.getId()).get();
        // Disconnect from session so that the updates on updatedPlanPassation are not directly saved in db
        em.detach(updatedPlanPassation);
        updatedPlanPassation
            .dateDebut(UPDATED_DATE_DEBUT)
            .dateFin(UPDATED_DATE_FIN)
            .commentaire(UPDATED_COMMENTAIRE)
            .annee(UPDATED_ANNEE);
        PlanPassationDTO planPassationDTO = planPassationMapper.toDto(updatedPlanPassation);

        restPlanPassationMockMvc.perform(put("/api/plan-passations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planPassationDTO)))
            .andExpect(status().isOk());

        // Validate the PlanPassation in the database
        List<PlanPassation> planPassationList = planPassationRepository.findAll();
        assertThat(planPassationList).hasSize(databaseSizeBeforeUpdate);
        PlanPassation testPlanPassation = planPassationList.get(planPassationList.size() - 1);
        assertThat(testPlanPassation.getDateDebut()).isEqualTo(UPDATED_DATE_DEBUT);
        assertThat(testPlanPassation.getDateFin()).isEqualTo(UPDATED_DATE_FIN);
        assertThat(testPlanPassation.getCommentaire()).isEqualTo(UPDATED_COMMENTAIRE);
        assertThat(testPlanPassation.getAnnee()).isEqualTo(UPDATED_ANNEE);
    }

    @Test
    @Transactional
    public void updateNonExistingPlanPassation() throws Exception {
        int databaseSizeBeforeUpdate = planPassationRepository.findAll().size();

        // Create the PlanPassation
        PlanPassationDTO planPassationDTO = planPassationMapper.toDto(planPassation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPlanPassationMockMvc.perform(put("/api/plan-passations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(planPassationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PlanPassation in the database
        List<PlanPassation> planPassationList = planPassationRepository.findAll();
        assertThat(planPassationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePlanPassation() throws Exception {
        // Initialize the database
        planPassationRepository.saveAndFlush(planPassation);

        int databaseSizeBeforeDelete = planPassationRepository.findAll().size();

        // Delete the planPassation
        restPlanPassationMockMvc.perform(delete("/api/plan-passations/{id}", planPassation.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PlanPassation> planPassationList = planPassationRepository.findAll();
        assertThat(planPassationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
