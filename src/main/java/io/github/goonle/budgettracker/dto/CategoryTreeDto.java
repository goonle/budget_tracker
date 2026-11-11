package io.github.goonle.budgettracker.dto;

import java.util.List;

public record CategoryTreeDto(Long id, String name, int depth, List<CategoryTreeDto> children) {
}
