package io.github.goonle.budgettracker.service;

import io.github.goonle.budgettracker.dto.CategoryTreeDto;
import io.github.goonle.budgettracker.entity.Category;
import io.github.goonle.budgettracker.repository.CategoryRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /** All categories as a tree: top-level groups with their children, in id order. */
    public List<CategoryTreeDto> getTree() {
        List<Category> all = categoryRepository.findAll(Sort.by("id"));

        Map<Long, List<Category>> childrenByParentId = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return all.stream()
                .filter(c -> c.getParent() == null)
                .map(root -> toTree(root, childrenByParentId))
                .toList();
    }

    private CategoryTreeDto toTree(Category category, Map<Long, List<Category>> childrenByParentId) {
        List<CategoryTreeDto> children = childrenByParentId
                .getOrDefault(category.getId(), List.of())
                .stream()
                .map(child -> toTree(child, childrenByParentId))
                .toList();
        return new CategoryTreeDto(category.getId(), category.getName(), category.getDepth(), children);
    }
}
