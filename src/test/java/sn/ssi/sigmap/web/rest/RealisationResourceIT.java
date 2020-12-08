package sn.ssi.sigmap.web.rest;

import sn.ssi.sigmap.PlanpassationmsApp;
import sn.ssi.sigmap.config.TestSecurityConfiguration;
import sn.ssi.sigmap.domain.Realisation;
import sn.ssi.sigmap.domain.PlanPassation;
import sn.ssi.sigmap.repository.RealisationRepository;
import sn.ssi.sigmap.service.RealisationService;
import sn.ssi.sigmap.service.dto.RealisationDTO;
import sn.ssi.sigmap.service.mapper.RealisationMapper;

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
 * Integration tests for the {@link RealisationResource} REST controller.
 */
@SpringBootTest(classes = { PlanpassationmsApp.class, TestSecurityConfiguration.class })
@AutoConfigureMockMvc
@WithMockUser
public class RealisationResourceIT {

    private static final String DEFAULT_LIBELLE = "AAAAAAAAAA";
    private static final String UPDATED_LIBELLE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DATE_ATTRIBUTION = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_ATTRIBUTION = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_DELAIEXECUTION = 1;
    private static final Integer UPDATED_DELAIEXECUTION = 2;

    @Autowired
    private RealisationRepository realisationRepository;

    @Autowired
    private RealisationMapper realisationMapper;

    @Autowired
    private RealisationService realisationService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRealisationMockMvc;

    private Realisation realisation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Realisation createEntity(EntityManager em) {
        Realisation realisation = new Realisation()
            .libelle(DEFAULT_LIBELLE)
            .dateAttribution(DEFAULT_DATE_ATTRIBUTION)
            .delaiexecution(DEFAULT_DELAIEXECUTION);
        // Add required entity
        PlanPassation planPassation;
        if (TestUtil.findAll(em, PlanPassation.class).isEmpty()) {
            planPassation = PlanPassationResourceIT.createEntity(em);
            em.persist(planPassation);
            em.flush();
        } else {
            planPassation = TestUtil.findAll(em, PlanPassation.class).get(0);
        }
        realisation.setPlanPassation(planPassation);
        return realisation;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Realisation createUpdatedEntity(EntityManager em) {
        Realisation realisation = new Realisation()
            .libelle(UPDATED_LIBELLE)
            .dateAttribution(UPDATED_DATE_ATTRIBUTION)
            .delaiexecution(UPDATED_DELAIEXECUTION);
        // Add required entity
        PlanPassation planPassation;
        if (TestUtil.findAll(em, PlanPassation.class).isEmpty()) {
            planPassation = PlanPassationResourceIT.createUpdatedEntity(em);
            em.persist(planPassation);
            em.flush();
        } else {
            planPassation = TestUtil.findAll(em, PlanPassation.class).get(0);
        }
        realisation.setPlanPassation(planPassation);
        return realisation;
    }

    @BeforeEach
    public void initTest() {
        realisation = createEntity(em);
    }

    @Test
    @Transactional
    public void createRealisation() throws Exception {
        int databaseSizeBeforeCreate = realisationRepository.findAll().size();
        // Create the Realisation
        RealisationDTO realisationDTO = realisationMapper.toDto(realisation);
        restRealisationMockMvc.perform(post("/api/realisations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(realisationDTO)))
            .andExpect(status().isCreated());

        // Validate the Realisation in the database
        List<Realisation> realisationList = realisationRepository.findAll();
        assertThat(realisationList).hasSize(databaseSizeBeforeCreate + 1);
        Realisation testRealisation = realisationList.get(realisationList.size() - 1);
        assertThat(testRealisation.getLibelle()).isEqualTo(DEFAULT_LIBELLE);
        assertThat(testRealisation.getDateAttribution()).isEqualTo(DEFAULT_DATE_ATTRIBUTION);
        assertThat(testRealisation.getDelaiexecution()).isEqualTo(DEFAULT_DELAIEXECUTION);
    }

    @Test
    @Transactional
    public void createRealisationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = realisationRepository.findAll().size();

        // Create the Realisation with an existing ID
        realisation.setId(1L);
        RealisationDTO realisationDTO = realisationMapper.toDto(realisation);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRealisationMockMvc.perform(post("/api/realisations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(realisationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Realisation in the database
        List<Realisation> realisationList = realisationRepository.findAll();
        assertThat(realisationList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkDateAttributionIsRequired() throws Exception {
        int databaseSizeBeforeTest = realisationRepository.findAll().size();
        // set the field null
        realisation.setDateAttribution(null);

        // Create the Realisation, which fails.
        RealisationDTO realisationDTO = realisationMapper.toDto(realisation);


        restRealisationMockMvc.perform(post("/api/realisations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(realisationDTO)))
            .andExpect(status().isBadRequest());

        List<Realisation> realisationList = realisationRepository.findAll();
        assertThat(realisationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDelaiexecutionIsRequired() throws Exception {
        int databaseSizeBeforeTest = realisationRepository.findAll().size();
        // set the field null
        realisation.setDelaiexecution(null);

        // Create the Realisation, which fails.
        RealisationDTO realisationDTO = realisationMapper.toDto(realisation);


        restRealisationMockMvc.perform(post("/api/realisations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(realisationDTO)))
            .andExpect(status().isBadRequest());

        List<Realisation> realisationList = realisationRepository.findAll();
        assertThat(realisationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRealisations() throws Exception {
        // Initialize the database
        realisationRepository.saveAndFlush(realisation);

        // Get all the realisationList
        restRealisationMockMvc.perform(get("/api/realisations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(realisation.getId().intValue())))
            .andExpect(jsonPath("$.[*].libelle").value(hasItem(DEFAULT_LIBELLE)))
            .andExpect(jsonPath("$.[*].dateAttribution").value(hasItem(DEFAULT_DATE_ATTRIBUTION.toString())))
            .andExpect(jsonPath("$.[*].delaiexecution").value(hasItem(DEFAULT_DELAIEXECUTION)));
    }
    
    @Test
    @Transactional
    public void getRealisation() throws Exception {
        // Initialize the database
        realisationRepository.saveAndFlush(realisation);

        // Get the realisation
        restRealisationMockMvc.perform(get("/api/realisations/{id}", realisation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(realisation.getId().intValue()))
            .andExpect(jsonPath("$.libelle").value(DEFAULT_LIBELLE))
            .andExpect(jsonPath("$.dateAttribution").value(DEFAULT_DATE_ATTRIBUTION.toString()))
            .andExpect(jsonPath("$.delaiexecution").value(DEFAULT_DELAIEXECUTION));
    }
    @Test
    @Transactional
    public void getNonExistingRealisation() throws Exception {
        // Get the realisation
        restRealisationMockMvc.perform(get("/api/realisations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRealisation() throws Exception {
        // Initialize the database
        realisationRepository.saveAndFlush(realisation);

        int databaseSizeBeforeUpdate = realisationRepository.findAll().size();

        // Update the realisation
        Realisation updatedRealisation = realisationRepository.findById(realisation.getId()).get();
        // Disconnect from session so that the updates on updatedRealisation are not directly saved in db
        em.detach(updatedRealisation);
        updatedRealisation
            .libelle(UPDATED_LIBELLE)
            .dateAttribution(UPDATED_DATE_ATTRIBUTION)
            .delaiexecution(UPDATED_DELAIEXECUTION);
        RealisationDTO realisationDTO = realisationMapper.toDto(updatedRealisation);

        restRealisationMockMvc.perform(put("/api/realisations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(realisationDTO)))
            .andExpect(status().isOk());

        // Validate the Realisation in the database
        List<Realisation> realisationList = realisationRepository.findAll();
        assertThat(realisationList).hasSize(databaseSizeBeforeUpdate);
        Realisation testRealisation = realisationList.get(realisationList.size() - 1);
        assertThat(testRealisation.getLibelle()).isEqualTo(UPDATED_LIBELLE);
        assertThat(testRealisation.getDateAttribution()).isEqualTo(UPDATED_DATE_ATTRIBUTION);
        assertThat(testRealisation.getDelaiexecution()).isEqualTo(UPDATED_DELAIEXECUTION);
    }

    @Test
    @Transactional
    public void updateNonExistingRealisation() throws Exception {
        int databaseSizeBeforeUpdate = realisationRepository.findAll().size();

        // Create the Realisation
        RealisationDTO realisationDTO = realisationMapper.toDto(realisation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRealisationMockMvc.perform(put("/api/realisations").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(realisationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Realisation in the database
        List<Realisation> realisationList = realisationRepository.findAll();
        assertThat(realisationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRealisation() throws Exception {
        // Initialize the database
        realisationRepository.saveAndFlush(realisation);

        int databaseSizeBeforeDelete = realisationRepository.findAll().size();

        // Delete the realisation
        restRealisationMockMvc.perform(delete("/api/realisations/{id}", realisation.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Realisation> realisationList = realisationRepository.findAll();
        assertThat(realisationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
