package org.jrc.rodec.application;

import org.jrc.rodec.application.interfaces.TableRowService;
import org.jrc.rodec.controller.TableRowDTO;
import org.jrc.rodec.domain.TableCell;
import org.jrc.rodec.domain.TableRow;
import org.jrc.rodec.domain.repositories.TableRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class TableRowServiceImpl implements TableRowService {

    @Inject
    TableRepository tableRepository;

    @Override
    @Transactional
    public List<TableRowDTO> get5RandomRows() {
        //get unanswered table rows
        List<TableRow> tableRows = new ArrayList<>(
                tableRepository.getUnansweredTableRows()
        );

        for (TableRow tr: tableRows){
            for(TableCell rowEntry: tr.rowEntries())
                System.out.println(rowEntry);
        }
        return tableRows
                .stream()
                .map(DTOMapper::mapTableRowToRowDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateRows(List<TableRowDTO> updatedTableRows) {
        for (TableRowDTO tableRowDTO: updatedTableRows
             ) {
            Optional<TableRow> tableRow = tableRepository
                    .getTableRowByTableIdAndRowNumber(tableRowDTO.getTableId(), tableRowDTO.getRow());
            tableRow.ifPresent(
                    row -> row.assignColumnAndQuestion(tableRowDTO.getAnswerColumn(), tableRowDTO.getQuestion())
            );
            tableRepository.updateTableRow(tableRow.get());
        }
        return true;
    }
}