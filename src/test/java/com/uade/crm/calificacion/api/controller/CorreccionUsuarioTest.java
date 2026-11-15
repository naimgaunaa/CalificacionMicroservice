package com.uade.crm.calificacion.api.controller;

import com.uade.crm.calificacion.api.dto.CorreccionManualRequest;
import com.uade.crm.calificacion.application.facade.CalificacionFacade;
import com.uade.crm.calificacion.domain.model.Calificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Quien hace la correccion manual lo decide el orquestador: valida el JWT y manda X-Usuario. Ese valor manda sobre el
 * campo "usuario" del body, que cualquiera podria falsificar.
 */
class CorreccionUsuarioTest {

    private static final String CUERPO =
            "{\"nuevoNivel\":\"MUY_POTABLE\",\"motivo\":\"Aval bancario\",\"usuario\":\"alguien-inventado\"}";

    private CalificacionFacade facade;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        facade = mock(CalificacionFacade.class);
        when(facade.corregirManualmente(eq("lead-1"), any())).thenReturn(new Calificacion());
        mvc = MockMvcBuilders.standaloneSetup(new CalificacionController(facade)).build();
    }

    private CorreccionManualRequest requestEnviado() {
        ArgumentCaptor<CorreccionManualRequest> captor = ArgumentCaptor.forClass(CorreccionManualRequest.class);
        verify(facade).corregirManualmente(eq("lead-1"), captor.capture());
        return captor.getValue();
    }

    @Test
    void elUsuarioDelHeaderPisaAlDelBody() throws Exception {
        mvc.perform(post("/calificaciones/lead-1/correcciones")
                        .header("X-Usuario", "ana@crm.test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isCreated());

        assertThat(requestEnviado().getUsuario()).isEqualTo("ana@crm.test");
    }

    @Test
    void sinHeaderSeUsaElDelBodyComoAntes() throws Exception {
        mvc.perform(post("/calificaciones/lead-1/correcciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isCreated());

        assertThat(requestEnviado().getUsuario()).isEqualTo("alguien-inventado");
    }

    @Test
    void unHeaderEnBlancoNoPisaAlBody() throws Exception {
        mvc.perform(post("/calificaciones/lead-1/correcciones")
                        .header("X-Usuario", "  ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isCreated());

        assertThat(requestEnviado().getUsuario()).isEqualTo("alguien-inventado");
    }
}
