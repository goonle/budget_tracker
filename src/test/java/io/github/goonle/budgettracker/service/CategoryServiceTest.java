package io.github.goonle.budgettracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.goonle.budgettracker.dto.CategoryTreeDto;
import io.github.goonle.budgettracker.entity.Category;
import io.github.goonle.budgettracker.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @InjectMocks CategoryService categoryService;

    @Test
    void getTree_buildsGroupsWithTheirChildren() {
        Category living = category(1L, "Living", null);
        Category food = category(2L, "Food", null);
        Category rent = category(3L, "Rent", living);
        Category power = category(4L, "Power", living);
        Category grocery = category(5L, "Grocery", food);
        when(categoryRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(living, food, rent, power, grocery));

        List<CategoryTreeDto> tree = categoryService.getTree();

        assertThat(tree).extracting(CategoryTreeDto::name).containsExactly("Living", "Food");
        assertThat(tree.get(0).children()).extracting(CategoryTreeDto::name).containsExactly("Rent", "Power");
        assertThat(tree.get(1).children()).extracting(CategoryTreeDto::name).containsExactly("Grocery");
    }

    @Test
    void getTree_groupWithoutChildren_hasEmptyChildren() {
        when(categoryRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(category(1L, "Uncategorised", null)));

        assertThat(categoryService.getTree().get(0).children()).isEmpty();
    }

    @Test
    void getTree_noCategories_returnsEmpty() {
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of());

        assertThat(categoryService.getTree()).isEmpty();
    }

    private static Category category(Long id, String name, Category parent) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setParent(parent);
        c.setDepth(parent == null ? 0 : parent.getDepth() + 1);
        return c;
    }
}
