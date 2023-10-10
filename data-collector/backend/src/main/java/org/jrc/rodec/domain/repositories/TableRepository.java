package org.jrc.rodec.domain.repositories;

import org.jrc.rodec.domain.TableRow;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TableRepository {
    Set<TableRow> getAllTableRows();

    Set<TableRow> getUnansweredTableRows();

    void updateTableRow(TableRow updatedTableRow);

    Optional<TableRow> getTableRowByTableIdAndRowNumber(String tableId, Integer row);


    void insertTable(List<TableRow> table);
}
