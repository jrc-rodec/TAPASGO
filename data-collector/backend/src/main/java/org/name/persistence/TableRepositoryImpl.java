package org.name.persistence;

import org.name.domain.TableRow;
import org.name.domain.repositories.TableRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TableRepositoryImpl implements TableRepository {

    @Inject
    EntityManager em;

    @Override
    @Transactional
    public Set<TableRow> getAllTableRows() {
        TypedQuery<TableRow> query = em.createQuery(
                        "FROM TableRow", TableRow.class
        );
        return query.getResultStream().collect(Collectors.toSet());
    }

    @Transactional
    public List<TableRow> getTableById(String tableId){
        TypedQuery<TableRow> query = em.createQuery(
                "FROM TableRow " +
                        "WHERE tableId = :tableId", TableRow.class
        );
        query.setParameter("tableId", tableId);
        return query.getResultStream().collect(Collectors.toList());
    }

    @Transactional
    public void insertTable(List<TableRow> table){
        int i = 0;
        if(table.size() > 0) {
            if (getTableById(table.get(0).tableId()).size() == 0)
                for (TableRow tableRow : table
                ) {
                    em.persist(tableRow);
                    if (++i > 10)
                        break;
                }
        }
    }

    @Override
    public Optional<TableRow> getTableRowByTableIdAndRowNumber(String tableId, Integer row) {
        TypedQuery<TableRow> query = em.createQuery(
                "FROM TableRow " +
                        "WHERE TABLE_ID = :tableId AND ROW = :row", TableRow.class
        );
        query.setParameter("tableId", tableId);
        query.setParameter("row", row);
        return query.getResultStream().findAny();
    }

    @Override
    @Transactional
    public Set<TableRow> getUnansweredTableRows() {
        TypedQuery<TableRow> query = em.createQuery(
                "FROM TableRow WHERE isAnswered = :isAnswered " +
                        "ORDER BY RAND()", TableRow.class
        );
        query.setParameter("isAnswered", false);
        query.setMaxResults(5);
        return query.getResultStream().collect(Collectors.toSet());

    }

    @Override
    @Transactional
    public void updateTableRow(TableRow updatedTableRow) {
        em.merge(updatedTableRow);
    }
}
