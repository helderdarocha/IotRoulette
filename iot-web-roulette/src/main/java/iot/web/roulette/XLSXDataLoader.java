package iot.web.roulette;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Carrega dados de arquivo XSLX usando Apache POI
 *
 */
public class XLSXDataLoader implements DataLoader {

	/**
	 * Dados do sorteio precisam estar em arquivo "dados.xlsx e neste pacote
	 * TODO - usar mecanismo mais flexivel: permitir upload do XLSX ou CSV webapp ou ler arquivo de pasta comum
	 */
	public static final String DADOS = "dados.xlsx"; // arquivo de dados (coloque no mesmo pacote)

	/** 
	 * Coluna excel onde estão os dados a serem usados no sorteio 
	 */
	public static final int DATA_COL = 0; 

	private List<String> tickets = new ArrayList<>();

	public XLSXDataLoader() throws IOException {
		InputStream is = this.getClass().getResourceAsStream(DADOS);
		
		if(is == null) { 
			throw new IOException("Não foi possível produzir um stream de dados de entrada - verifique que o arquivo existe, tem permissão de acesso e está no local correto");
		}
		
		load(is);
	}

	/**
	 * Carrega dados de arquivo XLSX (Excel)
	 */
	@Override
	public void load(InputStream stream) throws IOException {
		try (InputStream is = stream; Workbook workbook = new XSSFWorkbook(is)) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row != null) {
					String cellValue = null;
					for (int colIndex = 0; colIndex <= DATA_COL; colIndex++) {
						if (colIndex == DATA_COL) {
							Cell cell = row.getCell(colIndex);
							if (cell != null) {
								cellValue = formatter.formatCellValue(cell);
								tickets.add(cellValue); // adiciona esta célula na lista
								// falta checar se item não é um cabeçalho
								break;
							}
						}
					}
				}
			}
		}
		
		Collections.shuffle(tickets);
		
	}

	@Override
	public void addItem(String item) {
		throw new UnsupportedOperationException("Não implementado");

	}

	@Override
	public String[] getData() {
		return tickets.toArray(new String[tickets.size()]);
	}

}
