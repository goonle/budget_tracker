package io.github.goonle.budgettracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.goonle.budgettracker.entity.Category;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // example
    // @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    // Optional<Category> findByIdWithChildren(@Param("id") Long id);
    Optional<Category> findById(Long id);
    @Query("""
        WITH RECURSIVE category_tree AS (
            SELECT c.depth, c.id, c.name, c.parent_id, c.created_at, c.updated_at,
                ARRAY[c.id] AS path
            FROM category c
            WHERE c.id = :id
            UNION ALL
            SELECT p.depth, p.id, p.name, p.parent_id, p.created_at, p.updated_at,
                ct.path || p.id
            FROM category p
            JOIN category_tree ct ON p.id = ct.parent_id
            WHERE p.id <> ALL(ct.path)
        )
        SELECT ct.depth, ct.id, ct.name, ct.parent_id, ct.created_at, ct.updated_at
        FROM category_tree ct
        ORDER BY ct.depth
        """,
        nativeQuery = true )
    List<Category> findAncestryById(@Param("id") Long id);
    List<Category> findChildrenById(Long id);
    @Query("SELECT c FROM Category c ")
    List<Category> findParentAndChildById(Long id);
    List<Category> findSiblingsById(Long id);
}