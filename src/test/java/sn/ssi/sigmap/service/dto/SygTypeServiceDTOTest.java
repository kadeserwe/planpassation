package sn.ssi.sigmap.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import sn.ssi.sigmap.web.rest.TestUtil;

public class SygTypeServiceDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SygTypeServiceDTO.class);
        SygTypeServiceDTO sygTypeServiceDTO1 = new SygTypeServiceDTO();
        sygTypeServiceDTO1.setId(1L);
        SygTypeServiceDTO sygTypeServiceDTO2 = new SygTypeServiceDTO();
        assertThat(sygTypeServiceDTO1).isNotEqualTo(sygTypeServiceDTO2);
        sygTypeServiceDTO2.setId(sygTypeServiceDTO1.getId());
        assertThat(sygTypeServiceDTO1).isEqualTo(sygTypeServiceDTO2);
        sygTypeServiceDTO2.setId(2L);
        assertThat(sygTypeServiceDTO1).isNotEqualTo(sygTypeServiceDTO2);
        sygTypeServiceDTO1.setId(null);
        assertThat(sygTypeServiceDTO1).isNotEqualTo(sygTypeServiceDTO2);
    }
}
