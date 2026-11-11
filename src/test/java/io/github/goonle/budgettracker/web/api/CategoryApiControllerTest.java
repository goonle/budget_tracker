package io.github.goonle.budgettracker.web.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.goonle.budgettracker.dto.CategoryTreeDto;
import io.github.goonle.budgettracker.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryApiController.class)
class CategoryApiControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CategoryService categoryService;

    @Test
    void getTree_returnsTreeAsJson() throws Exception {
        when(categoryService.getTree()).thenReturn(List.of(
                new CategoryTreeDto(1L, "Living", 0, List.of(
                        new CategoryTreeDto(3L, "Rent", 1, List.of())))));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Living"))
                .andExpect(jsonPath("$[0].children[0].name").value("Rent"))
                .andExpect(jsonPath("$[0].children[0].depth").value(1));
    }
}
