package io.github.goonle.budgettracker.repository;

import io.github.goonle.budgettracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // example
    // @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    // Optional<Category> findByIdWithChildren(@Param("id") Long id);

    //Find parents including itself by id
    @Query(value = """
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
        nativeQuery = true)
    List<Category> findAncestryById(@Param("id") Long id);

    //Find childeren excluding itself by id
    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT c.id, c.name, c.parent_id, c.depth, c.created_at, c.updated_at,
                ARRAY[:id, c.id] AS path
            FROM category c
            WHERE c.parent_id = :id
            UNION ALL
            SELECT ch.id, ch.name, ch.parent_id, ch.depth, ch.created_at, ch.updated_at,
                ct.path || ch.id
            FROM category ch
            JOIN category_tree ct ON ch.parent_id = ct.id
            WHERE ch.id <> ALL(ct.path)
        )
        SELECT depth, id, name, parent_id, created_at, updated_at
        FROM category_tree
        ORDER BY path
        """,
        nativeQuery = true)
    List<Category> findChildrenById(@Param("id") Long id);


    @Query(value = """
        WITH RECURSIVE category_tree_up AS (
            SELECT c.id, c.name, c.parent_id, c.depth, c.created_at, c.updated_at,
                ARRAY[c.id] AS path
            FROM category c
            WHERE c.id = :id
            UNION ALL
            SELECT p.id, p.name, p.parent_id, p.depth, p.created_at, p.updated_at,
                ctu.path || p.id
            FROM category p
            JOIN category_tree_up ctu ON p.id = ctu.parent_id
            WHERE p.id <> ALL(ctu.path)
        ),
        category_tree_down AS (
            SELECT c.id, c.name, c.parent_id, c.depth, c.created_at, c.updated_at,
                ARRAY[CAST(:id AS bigint), c.id] AS path
            FROM category c
            WHERE c.parent_id = :id
            UNION ALL
            SELECT ch.id, ch.name, ch.parent_id, ch.depth, ch.created_at, ch.updated_at,
                ctd.path || ch.id
            FROM category ch
            JOIN category_tree_down ctd ON ch.parent_id = ctd.id
            WHERE ch.id <> ALL(ctd.path)
        )
        SELECT depth, id, name, parent_id, created_at, updated_at
        FROM category_tree_up
        UNION ALL
        SELECT depth, id, name, parent_id, created_at, updated_at
        FROM category_tree_down
        ORDER BY depth, id
    """,
        nativeQuery = true)
    List<Category> findParentAndChildById(@Param("id") Long id);

    List<Category> findByParentId(@Param("id") Long id);
}