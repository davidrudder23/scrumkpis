package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import models.Employee;
import models.OpenTicketLog;
import models.ScrumMaster;
import models.Sprint;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import utils.AuthenticationUtil;
import utils.StringUtils;

@Authenticated(AuthenticationUtil.class)
public class ReportController extends ParentController {

	public static Result index() {
		return ok(views.html.ReportController.index.render());
	}
	
	public static Result velocityChart() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		List<Sprint> sprints = scrumMaster.getSprints();
		Collections.sort(sprints, new Comparator<Sprint>() {

			@Override
			public int compare(Sprint sprintA, Sprint sprintB) {
				if ((sprintA==null) && (sprintB==null)) {
					return 0;
				}
				if (sprintA==null) {
					return 1;
				}
				if (sprintB==null) {
					return -1;
				}
				
				if ((sprintA.startDate==null) && (sprintB.startDate==null)) {
					return 0;
				}
				if (sprintA.startDate==null) {
					return 1;
				}
				if (sprintB.startDate==null) {
					return -1;
				}
				return sprintA.startDate.compareTo(sprintB.startDate);
			}
			
		});
		return ok(views.html.ReportController.velocityChart.render(sprints));
	}
	
	public static Result individualVelocityChart() {
		if (!Authentication.isScrumMaster()) {
			redirect(routes.ReportController.index());
		}


		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		List<Sprint> sprints = scrumMaster.getSprints();
		Collections.sort(sprints, new Comparator<Sprint>() {

			@Override
			public int compare(Sprint sprintA, Sprint sprintB) {
				if ((sprintA==null) && (sprintB==null)) {
					return 0;
				}
				if (sprintA==null) {
					return 1;
				}
				if (sprintB==null) {
					return -1;
				}
				
				if ((sprintA.startDate==null) && (sprintB.startDate==null)) {
					return 0;
				}
				if (sprintA.startDate==null) {
					return 1;
				}
				if (sprintB.startDate==null) {
					return -1;
				}
				return sprintA.startDate.compareTo(sprintB.startDate);
			}
			
		});
		
		List<Employee> employees = new ArrayList<Employee>();
		for (Sprint sprint: sprints) {
			for (Employee employee: sprint.getEmployees()) {
				if (!employees.contains(employee)) {
					employees.add(employee);
				}
			}
		}
		Logger.debug("Employees="+employees.toString());
		return ok(views.html.ReportController.individualVelocityChart.render(sprints, employees));
	}
	
	public static Result openTicketsChart() {
		ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
		List<OpenTicketLog> openTickets = OpenTicketLog.find.where().eq("scrumMaster", scrumMaster).findList();
		Collections.sort(openTickets, new Comparator<OpenTicketLog>() {

			@Override
			public int compare(OpenTicketLog o1, OpenTicketLog o2) {
				return o1.date.compareTo(o2.date);
			}
			
		});
		return ok(views.html.ReportController.openTicketsChart.render(openTickets));
	}
	
	public static Result excel() {
		try {
			ScrumMaster scrumMaster = Authentication.getLoggedInScrumMaster();
			Workbook wb = new HSSFWorkbook(ReportController.class.getClassLoader().getResourceAsStream("scrumkpis_worksheet.xls"));
			Sheet sheet = wb.getSheet("Raw Data");
			if (sheet == null) {
				sheet = wb.createSheet("Raw Data");
			}
			
			List<Sprint> sprints = scrumMaster.getSprints();
			Collections.sort(sprints, new Comparator<Sprint>() {

				@Override
				public int compare(Sprint sprintA, Sprint sprintB) {
					if ((sprintA==null) && (sprintB==null)) {
						return 0;
					}
					if (sprintA==null) {
						return 1;
					}
					if (sprintB==null) {
						return -1;
					}
					
					if ((sprintA.startDate==null) && (sprintB.startDate==null)) {
						return 0;
					}
					if (sprintA.startDate==null) {
						return 1;
					}
					if (sprintB.startDate==null) {
						return -1;
					}
					return sprintA.startDate.compareTo(sprintB.startDate);
				}
				
			});
			
			int cellNum = 0;
			Row row = sheet.createRow((short)0);
			row.createCell(cellNum++).setCellValue("Sprint");
			row.createCell(cellNum++).setCellValue("Num Available");
			row.createCell(cellNum++).setCellValue("Num Completed");
			row.createCell(cellNum++).setCellValue("Percent Completed");
			row.createCell(cellNum++).setCellValue("Reopens");
			int rowNum = 1;
			for (Sprint sprint: sprints) {
				cellNum = 0;
				// Create a row and put some cells in it. Rows are 0 based.
				row = sheet.createRow((short)rowNum);
				row.createCell(cellNum++).setCellValue(sprint.name);
				row.createCell(cellNum++).setCellValue(sprint.getTotalStoryPointsPromised());
				row.createCell(cellNum++).setCellValue(sprint.getTotalStoryPointsCompleted());
				row.createCell(cellNum++).setCellValue((sprint.getTotalStoryPointsCompleted()*100)/sprint.getTotalStoryPointsPromised());
				row.createCell(cellNum++).setCellValue(sprint.getTotalReopens());
				
				rowNum++;
			}
			
			// Clear out 300 rows.  We need to do this because
			// POI can't modify chart ranges, so the template has 300 rows
			for (int x = rowNum; x < 300; x++) {
				cellNum = 0;
				// Create a row and put some cells in it. Rows are 0 based.
				row = sheet.createRow((short)rowNum);
				row.removeCell(row.createCell(cellNum++));
				row.removeCell(row.createCell(cellNum++));
				row.removeCell(row.createCell(cellNum++));
				row.removeCell(row.createCell(cellNum++));
				row.removeCell(row.createCell(cellNum++));
				
				rowNum++;
			}
			
			// Write the output to a file
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String filename = "/tmp/scrumkpis_"+scrumMaster.id+"_"+sdf.format(new Date())+".xls";
			FileOutputStream fileOut = new FileOutputStream(filename);
			wb.write(fileOut);
			fileOut.close();
			return ok(new File(filename));
		} catch (Exception anyExc) {
			Logger.warn("Can't write excel workbook", anyExc);
		}
				    
		return ok();
	}
}
