package org.jrc.rodec.application.interfaces;

import org.jrc.rodec.controller.TableRowDTO;

import java.util.List;

public interface TableRowService {

    List<TableRowDTO> get5RandomRows();

    boolean updateRows(List<TableRowDTO> updatedTableRows);

}
