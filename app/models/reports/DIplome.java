package models.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.ModelException;
import models.Utils;
import models.entities.Card;
import models.entities.CourseWorkMark;
import models.entities.FinalMark;
import models.entities.GosMark;
import models.entities.Person;
import models.entities.PracticMark;
import models.entities.Renaming;
import models.entities.School;
import models.entities.Speciality;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Класс, генерирующий отчет в виде pdf документа. Поскольку планируется
 * генерить относительно простые отчеты, класс использует поток для записи в
 * память. Думаю, это быстрее файлового ввода-вывода.
 * 
 * @author Воронин Леонид
 * 
 */
public class DIplome {
	// поток байт в котором будет "собираться" отчет.
	private ByteArrayOutputStream stream;
	private BaseFont baseFont;
	private Font regularFont;
	private Font smallFont;

	/**
	 * Преобразует миллиметры в пункты из расчета, что один пункт равен 1/72
	 * дюйма.
	 * 
	 * @param milimeters
	 *            миллиметры
	 * @return дробное число пунктов
	 */
	private float getPt(float milimeters) {
		return milimeters * 72 / 25.4f;
	}

	/**
	 * Изготавливает "обертку" для элемента, помещая его в таблицу с одной
	 * ячейкой.
	 * 
	 * @param element
	 *            - элемент для обертывания
	 * @param minHeight
	 *            - минимальная высота таблицы-обертки
	 * @return объект типа PdfPTable
	 */
	private PdfPTable wrapElement(final Element element, final float minHeight) {
		PdfPTable wrapperTable = new PdfPTable(1);
		wrapperTable.setWidthPercentage(100.0f);
		PdfPCell wrapperCell = new PdfPCell();
		wrapperCell.setBorder(PdfPCell.NO_BORDER);
		wrapperCell.setMinimumHeight(minHeight);
		wrapperCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		wrapperCell.addElement(element);
		wrapperTable.addCell(wrapperCell);
		return wrapperTable;
	}

	/**
	 * Готовит параграф с требуемым выравниванием, текстом и шрифтом
	 * 
	 * @param text
	 *            текст
	 * @param font
	 *            шрифт
	 * @param alignment
	 *            выравнивание
	 * @return объект типа paragraph
	 */
	private Paragraph getParagraph(String text, Font font, int alignment) {
		Paragraph result = new Paragraph(text, font);
		result.setAlignment(alignment);
		result.setLeading(font.getSize() * 1.1f);
		return result;
	}

	/**
	 * Выводит текст в заданные координаты относительно левого нижнего угла
	 * 
	 * @param canvas
	 *            канва документа
	 * @param font
	 *            используемый шрифт
	 * @param text
	 *            текст
	 * @param x
	 *            координата X в миллиметрах
	 * @param y
	 *            координата Y в миллиметрах
	 */
	private void putText(final PdfContentByte canvas, final Font font,
			final String text, final float x, final float y) {
		canvas.saveState();
		canvas.beginText();
		canvas.moveText(getPt(x), getPt(y));
		canvas.setFontAndSize(font.getBaseFont(), font.getSize());
		canvas.showText(text);
		canvas.endText();
		canvas.restoreState();
	}

	/**
	 * Подготавливает шрифты для использования в документе
	 * @param regularSize размер обычного шрифта
	 * @param smallSize размер маленького шрифта
	 * @throws IOException
	 * @throws DocumentException
	 */
	private void prepareFonts(final int regularSize, final int smallSize)
			throws IOException, DocumentException {
		baseFont = BaseFont.createFont("fonts/times.ttf", BaseFont.IDENTITY_H,
				BaseFont.EMBEDDED);
		regularFont = new Font(baseFont, regularSize);
		smallFont = new Font(baseFont, smallSize);
	}

	/**
	 * Готовит таблицу оценок за курсовые проекты
	 * 
	 * @param marks
	 * @return
	 * @throws DocumentException
	 */
	private PdfPTable prepareCourseWorkTable(final List<CourseWorkMark> marks)
			throws DocumentException {
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100.0f);
		table.setWidths(new int[] { 12, 2 });
		PdfPCell nameCell;
		PdfPCell markCell;
		for (CourseWorkMark mark : marks) {
			nameCell = new PdfPCell(getParagraph(mark.getSubject() + 
					" (" + mark.theme + ")",
					smallFont, Paragraph.ALIGN_LEFT));
			nameCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
//			nameCell.setPaddingRight(getPt(3));
//			nameCell.setLeading(0.5f, 0.6f);
			nameCell.setBorder(PdfPCell.NO_BORDER);
			markCell = new PdfPCell(getParagraph(Utils.getMarkString(mark.mark),
					smallFont, Paragraph.ALIGN_CENTER));
			markCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
//			markCell.setLeading(0.5f, 0.6f);
			markCell.setBorder(PdfPCell.NO_BORDER);
			table.addCell(nameCell);
			table.addCell(markCell);
		}
		return table;
	}

	private void prepareMarkTables(final List<MarkItem> marks,
			PdfPTable table1, PdfPTable table2) throws DocumentException {

		boolean firstPage = true;
		PdfPCell nameCell;
		PdfPCell hoursCell;
		PdfPCell markCell;
		for (MarkItem mark : marks) {
			nameCell = new PdfPCell(getParagraph(mark.subject,
					smallFont, Paragraph.ALIGN_LEFT));
			nameCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			nameCell.setPaddingRight(getPt(3));
			nameCell.setLeading(0.5f, 0.7f);
			nameCell.setBorder(PdfPCell.NO_BORDER);
			hoursCell = new PdfPCell(getParagraph(mark.load, smallFont, Paragraph.ALIGN_CENTER));
			hoursCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			hoursCell.setLeading(0.5f, 0.7f);
			hoursCell.setBorder(PdfPCell.NO_BORDER);
			markCell = new PdfPCell(getParagraph(mark.mark, smallFont, Paragraph.ALIGN_CENTER));
			markCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			markCell.setLeading(0.5f, 0.7f);
			markCell.setBorder(PdfPCell.NO_BORDER);
			if (firstPage) {
				table1.addCell(nameCell);
				table1.addCell(hoursCell);
				table1.addCell(markCell);
				if (table1.calculateHeights() > 460) {
						// Опа! высота таблицы зашкаливает. Удаляем последнюю строку
						firstPage = false;
						table1.deleteLastRow();
					}
			}
			if (!firstPage) {
				// Выводим данные на вторую страницу
				table2.addCell(nameCell);
				table2.addCell(hoursCell);
				table2.addCell(markCell);
			}
		}
	}

	/**
	 * Готовит таблицу переименований учебного заведения.
	 * 
	 * @param renamingList
	 * @return
	 * @throws DocumentException
	 */
	private PdfPTable prepareRenamingTable(List<Renaming> renamingList)
			throws DocumentException {
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(100.0f);
		PdfPCell cell = null;
		for (Renaming item : renamingList) {
			cell = new PdfPCell();
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.addElement(getParagraph(
					"Образовательная организация переименована в "
							+ Utils.getYear(item.renamingDate) + " году;", smallFont,
					Paragraph.ALIGN_LEFT));
			cell.addElement(getParagraph(
					"старое полное наименование образовательной организации: "
							+ item.oldName, smallFont,
					Paragraph.ALIGN_LEFT));
			table.addCell(cell);
		}
		return table;
	}

	/**
	 * Готовит отчет в виде pdf документа.
	 * 
	 * @throws ModelException
	 */
	public void build(final Card card, final boolean isCopy) throws ModelException {
		try {
			prepareFonts(10, 6);
			Document document = new Document(PageSize.A4.rotate(), 15f, 15f,
					75f, 15f);
			PdfWriter writer = PdfWriter.getInstance(document, stream);
			document.open();
			document.addTitle("Диплом о среднеспециальном образовании и приложение к нему.");
			document.addAuthor("getpdf project");
			
			// Данные для вывода (возможно лучше тут считать всё с карточки)
			School scl = card.getSchool();
			Speciality spc = card.getSpeciality();
			Person psn = card.getPerson();
			String sclName = scl.name + "\n" + scl.place;
			String comissionDate = "от " + Utils.getDateString(card.comissionDate) + " года";
			String diplomeDate = Utils.getDateString(card.diplomeDate) + " года";
			String birthDate = Utils.getDateString(psn.birthDate) + " года";
			String spo = "о среднем профессиональном образовании";
			String schoolDirector = scl.director;
			String comissionDirector = card.comissionDirector;
			String speciality = spc.getFullName();
			String oldDocument = card.documentName + ", " + 
					Utils.getYear(card.documentDate) + " год.";
			List<MarkItem> marks = new ArrayList<MarkItem>();
			// Добавляем итоговые оценки
			int aload = 0;
			int mload = 0;
			for (FinalMark fm: card.getFinalMarks()) {
				marks.add(new MarkItem(fm));
				if (!fm.isModule) {
					aload += fm.auditoryLoad;
					mload += fm.maximumLoad;
				} else {
					marks.add(new MarkItem("в том числе:", "", ""));
				}
			}
			marks.add(new MarkItem("ВСЕГО часов теоретического обучения:", mload, 0));
			marks.add(new MarkItem("в том числе аудиторных часов:", aload, 0));
			marks.add(new MarkItem("Практика", card.getPracticLoad(), 0));
			marks.add(new MarkItem("в том числе:", "", ""));
			// Добавляем оценки за практику
			for (PracticMark pm: card.getPracticMarks()) {
				marks.add(new MarkItem(pm));
			}
			marks.add(new MarkItem("Государственная итоговая аттестация", card.diplomeLength, 0));
			marks.add(new MarkItem("в том числе:", "", ""));
			String title = (card.gosExam)? "Итоговый междисциплинарный государственный экзамен" :
				("Дипломный проект на тему \"" + card.diplomeTheme + "\"");
			marks.add(new MarkItem(title, "x", Utils.getMarkString(card.diplomeMark)));
			for (GosMark gm: card.getGosMarks()) {
				marks.add(new MarkItem(gm));
			}
			

			// ============================================================
			// Бланк диплома
			// ============================================================
			PdfPTable mainTable = new PdfPTable(2);
			mainTable.setWidthPercentage(100.0f);

			// Первая колонка
			PdfPCell firstTableCell = new PdfPCell();
			firstTableCell.setPaddingLeft(20f);
			firstTableCell.setPaddingRight(10f);
			firstTableCell.setBorder(PdfPCell.NO_BORDER);

			// Отступ от верхнего края страницы
			firstTableCell.addElement(wrapElement(new Phrase(" ", regularFont), getPt(45)));
			// Наименование учебного заведения
			firstTableCell.addElement(wrapElement(getParagraph(sclName, regularFont, Paragraph.ALIGN_CENTER), 188));
			// Квалификация
			firstTableCell.addElement(wrapElement(getParagraph(spc.kvalification, regularFont, Paragraph.ALIGN_CENTER), 97));
			// Регистрационный номер
			firstTableCell.addElement(wrapElement(
					getParagraph(card.registrationNumber, regularFont,
							Paragraph.ALIGN_CENTER), 40));
			// Дата выдачи
			firstTableCell.addElement(wrapElement(
					getParagraph(diplomeDate, regularFont,
							Paragraph.ALIGN_CENTER), 40));

			// Добавляем первую колонку в основную таблицу. Страница диплома создана.
			mainTable.addCell(firstTableCell);
			// Вторая колонка
			PdfPCell secondTableCell = new PdfPCell();
			secondTableCell.setBorder(PdfPCell.NO_BORDER);
			// Добавляем данные
			// Отступ от верхнего края страницы
			secondTableCell.addElement(wrapElement(new Phrase(" ", regularFont), getPt(20)));
			// ФИО
			secondTableCell.addElement(wrapElement(getParagraph(psn.firstName, regularFont, Paragraph.ALIGN_CENTER), getPt(5)));
			secondTableCell.addElement(wrapElement(getParagraph(psn.middleName, regularFont, Paragraph.ALIGN_CENTER), getPt(5)));
			secondTableCell.addElement(wrapElement(getParagraph(psn.lastName, regularFont, Paragraph.ALIGN_CENTER), getPt(39)));
			// образовательная программа
			secondTableCell.addElement(wrapElement(getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER), getPt(30)));
			// Дата комиссии
			secondTableCell.addElement(wrapElement(getParagraph(comissionDate, regularFont, Paragraph.ALIGN_CENTER), getPt(21)));
			// Председатель комиссии
			secondTableCell.addElement(wrapElement(getParagraph(comissionDirector, regularFont, Paragraph.ALIGN_RIGHT), getPt(15)));
			// Руководитель организации
			secondTableCell.addElement(wrapElement(getParagraph(schoolDirector, regularFont, Paragraph.ALIGN_RIGHT), getPt(20)));
			
			mainTable.addCell(secondTableCell);
			// Добавляем подложку в виде картинки, если требуется копия выписки
			if (isCopy) {
				Image img = Image.getInstance(this.getClass().getClassLoader()
						.getResource("images/02.jpg"));
				img.scaleAbsolute(getPt(297), getPt(210));
				img.setAbsolutePosition(0, 0);
				document.add(img);
			}
			document.add(mainTable);
			// Добавляем приложение к диплому
			document.newPage();
			// ============================================================
			// Приложение к диплому
			// ============================================================
			// Страницы 1 и 4 приложения к диплому (сторона 1 листа)
			// Основная таблица из двух столбцов. Так мы реализуем колонки
			mainTable = new PdfPTable(2);
			mainTable.setWidthPercentage(100.0f);

			// Первая колонка (страница 4 приложения к диплому)
			firstTableCell = new PdfPCell();
			firstTableCell.setPaddingLeft(15f);
			firstTableCell.setPaddingRight(10f);
			firstTableCell.setBorder(PdfPCell.NO_BORDER);

			// Добавим таблицу курсовых в колонку
			PdfPTable courseWorkTable = wrapElement(
//					prepareCourseWorkTable(getCourseList()), 290.0f);
					prepareCourseWorkTable(card.getCourseWorkMarks()), 290.0f);
			firstTableCell.addElement(courseWorkTable);

			// Добавим дополнительные сведения в колонку
			PdfPTable renamingTable = wrapElement(
					prepareRenamingTable(Renaming.find(card.beginDate, card.endDate)), 80.0f);
//					prepareRenamingTable(getRenamingList(card.beginDate, card.endDate)), 80.0f);
			renamingTable.setSpacingBefore(20.0f);
			firstTableCell.addElement(renamingTable);

			// Добавим данные о руководителе организации
			firstTableCell.addElement(wrapElement(
					getParagraph(schoolDirector, regularFont,
							Paragraph.ALIGN_RIGHT), 50.0f));

			// Добавляем первую колонку в основную таблицу. Страница 4 создана.
			mainTable.addCell(firstTableCell);
			// Вторая колонка (страница 1 приложения к диплому)
			secondTableCell = new PdfPCell();
			secondTableCell.setBorder(PdfPCell.NO_BORDER);
			PdfPTable secondTable = new PdfPTable(2);
			secondTable.setWidthPercentage(100.0f);
			secondTable.setWidths(new int[] { 3, 6 });
			secondTable.setSpacingBefore(20f);
			// Маленькая колонка (под гербом РФ)
			PdfPCell innerCell1 = new PdfPCell();
			innerCell1.setBorder(PdfPCell.NO_BORDER);
			innerCell1.addElement(wrapElement(new Phrase("", smallFont), 100));
			innerCell1.addElement(wrapElement(getParagraph(sclName, regularFont, Paragraph.ALIGN_CENTER), 230));
			innerCell1.addElement(wrapElement(
					getParagraph(spo,
							regularFont, Paragraph.ALIGN_CENTER), 70));
			innerCell1.addElement(wrapElement(
					getParagraph(card.registrationNumber, regularFont,
							Paragraph.ALIGN_CENTER), 50));
			innerCell1.addElement(wrapElement(
					getParagraph(diplomeDate, regularFont,
							Paragraph.ALIGN_CENTER), 20));
			// Большая колонка (сведения об обладателе диплома и т.п.)
			PdfPCell innerCell2 = new PdfPCell();
			innerCell2.setBorder(PdfPCell.NO_BORDER);
			innerCell2.addElement(wrapElement(new Phrase("", smallFont), 20));
			innerCell2
					.addElement(wrapElement(
							getParagraph(psn.firstName, regularFont,
									Paragraph.ALIGN_CENTER), 50));
			innerCell2.addElement(wrapElement(
					getParagraph(psn.middleName, regularFont, Paragraph.ALIGN_CENTER),
					50));
			innerCell2.addElement(wrapElement(
					getParagraph(psn.lastName, regularFont,
							Paragraph.ALIGN_CENTER), 50));
			innerCell2.addElement(wrapElement(
					getParagraph(birthDate, regularFont,
							Paragraph.ALIGN_CENTER), 50));
			innerCell2.addElement(wrapElement(
					getParagraph(oldDocument,
							regularFont, Paragraph.ALIGN_LEFT), 130));
			innerCell2.addElement(wrapElement(
					getParagraph(spc.length, regularFont,
							Paragraph.ALIGN_CENTER), 50));
			innerCell2.addElement(wrapElement(
					getParagraph(spc.kvalification, regularFont,
							Paragraph.ALIGN_CENTER), 40));
			innerCell2
					.addElement(wrapElement(
							getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER), 30));
			// Добавляем колонки
			secondTable.addCell(innerCell1);
			secondTable.addCell(innerCell2);
			// Добавляем таблицу в колонку 2
			secondTableCell.addElement(secondTable);
			mainTable.addCell(secondTableCell);
			// Добавляем подложку в виде картинки, если требуется копия выписки
			if (isCopy) {
				Image img = Image.getInstance(this.getClass().getClassLoader()
						.getResource("images/03.jpg"));
				img.scaleAbsolute(getPt(297), getPt(210));
				img.setAbsolutePosition(0, 0);
				document.add(img);
			}
			document.add(mainTable);
			// Выведем номера страниц
			PdfContentByte canvas = writer.getDirectContent();
			putText(canvas, regularFont, "4", 54, 16); // Страниц всего
			putText(canvas, regularFont, "4", 27, 4); // Страница 4
			putText(canvas, regularFont, "1", 289, 4); // Страница 1

			// ===========================================================================
			// Сторона 1 приложения к диплому (страницы 1 и 4) сформированы. Приступаем к
			// стороне 2.
			document.newPage();
			mainTable = new PdfPTable(2);
			mainTable.setWidthPercentage(100.0f);
			// Первая колонка (страница 2 приложения к диплому)
			firstTableCell = new PdfPCell();
			firstTableCell.setPaddingLeft(15f);
			firstTableCell.setPaddingRight(15f);
			firstTableCell.setBorder(PdfPCell.NO_BORDER);
			firstTableCell.setMinimumHeight(300);
			PdfPTable firstTable = new PdfPTable(3);
			firstTable.setWidthPercentage(100.0f);
			firstTable.setTotalWidth(getPt(135));
			firstTable.setWidths(new int[] { 10, 2, 2 });
			firstTable.setSpacingBefore(35);
			// Вторая колонка (страница 1 приложения к диплому)
			secondTableCell = new PdfPCell();
			secondTableCell.setPaddingLeft(17f);
			secondTableCell.setPaddingRight(12f);
			secondTableCell.setBorder(PdfPCell.NO_BORDER);
			secondTableCell.setMinimumHeight(350);
			secondTable = new PdfPTable(3);
			secondTable.setWidthPercentage(100.0f);
			firstTable.setTotalWidth(getPt(135));
			secondTable.setWidths(new int[] { 10, 2, 2 });
			secondTable.setSpacingBefore(5);

			// Заполняем данными страницы 2 и 3
			prepareMarkTables(marks, firstTable, secondTable);
			firstTableCell.addElement(firstTable);
			secondTableCell.addElement(secondTable);
			mainTable.addCell(firstTableCell);
			mainTable.addCell(secondTableCell);
			// Если требуется копия выписки, то отображаем картинку в качестве подложки
			if (isCopy) {
				Image img = Image.getInstance(this.getClass().getClassLoader()
						.getResource("images/04.jpg"));
				img.scaleAbsolute(getPt(297), getPt(210));
				img.setAbsolutePosition(0, 0);
				document.add(img);
			}
			document.add(mainTable);
			// Выводим номера страниц
			canvas = writer.getDirectContent();
			putText(canvas, regularFont, "2", 27, 4); // Страница 4
			putText(canvas, regularFont, "3", 289, 4); // Страница 1
			// Закрываем документ
			document.close();
			// } catch (NullPointerException e) {
			// throw new ReportException(
			// "NullPointerException was occured while report creation process!");
		} catch (IOException | DocumentException e) {
			throw new ModelException(e.getMessage());
		}
	}

	/**
	 * Конструктор класса.
	 * 
	 * @throws ModelException
	 */
	public DIplome() throws ModelException {
		stream = new ByteArrayOutputStream();
	}

	/**
	 * Простой метод для проверки готовности отчета.
	 * 
	 * @return истина если размер данных отчета отличен от нуля. Иначе - ложь.
	 */
	public boolean isReady() {
		return (stream != null) && (stream.size() > 0);
	}

	/**
	 * Метод, отдающий содержимое отчета в виде массива байт.
	 * 
	 * @return массив байт (pdf файл побайтно).
	 * @throws ModelException
	 */
	public byte[] getReportContent() throws ModelException {
		if (isReady()) {
			return stream.toByteArray();
		}
		throw new ModelException("There is no data in the stream!");
	}
}
