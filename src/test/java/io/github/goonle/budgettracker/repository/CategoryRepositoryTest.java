package io.github.goonle.budgettracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.goonle.budgettracker.TestcontainersConfiguration;
import io.github.goonle.budgettracker.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CategoryRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired CategoryRepository repository;

    Category housing, utilities, water, gas, rent, food, groceries;

    @BeforeEach
    void setUp() {
        // "T-" prefix: top-level names must not clash with the seeded categories (V3).
        housing   = category("T-Housing", null);
        utilities = category("Utilities", housing);
        water     = category("Water", utilities);
        gas       = category("Gas", utilities);
        rent      = category("Rent", housing);
        food      = category("T-Food", null);
        groceries = category("Groceries", food);
        em.clear(); // make the repository read from the database, not the cache
    }

    @Test
    void findAncestryById_leaf_returnsRootToSelfInDepthOrder() {
        assertThat(names(repository.findAncestryById(water.getId())))
                .containsExactly("T-Housing", "Utilities", "Water");
    }

    @Test
    void findAncestryById_root_returnsOnlyItself() {
        assertThat(names(repository.findAncestryById(housing.getId())))
                .containsExactly("T-Housing");
    }

    @Test
    void findChildrenById_root_returnsAllDescendantsExcludingSelf() {
        assertThat(names(repository.findChildrenById(housing.getId())))
                .containsExactly("Utilities", "Water", "Gas", "Rent");
    }

    @Test
    void findChildrenById_leaf_returnsEmpty() {
        assertThat(repository.findChildrenById(water.getId())).isEmpty();
    }

    @Test
    void findParentAndChildById_middle_returnsAncestorsSelfAndDescendants() {
        assertThat(names(repository.findParentAndChildById(utilities.getId())))
                .containsExactly("T-Housing", "Utilities", "Water", "Gas");
    }

    @Test
    void findByParentId_returnsDirectChildrenOnly() {
        assertThat(names(repository.findByParentId(housing.getId())))
                .containsExactlyInAnyOrder("Utilities", "Rent");
    }

    @Test
    void findByParentId_leaf_returnsEmpty() {
        assertThat(repository.findByParentId(gas.getId())).isEmpty();
    }

    // ---- helpers ----

    private Category category(String name, Category parent) {
        Category c = new Category();
        c.setName(name);
        c.setParent(parent);
        c.setDepth(parent == null ? 0 : parent.getDepth() + 1);
        return em.persistAndFlush(c);
    }

    private static java.util.List<String> names(java.util.List<Category> categories) {
        return categories.stream().map(Category::getName).toList();
    }
}
