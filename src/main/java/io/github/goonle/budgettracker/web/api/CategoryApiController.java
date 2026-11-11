package io.github.goonle.budgettracker.web.api;

import io.github.goonle.budgettracker.dto.CategoryTreeDto;
import io.github.goonle.budgettracker.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryTreeDto> getTree() {
        return categoryService.getTree();
    }
}
