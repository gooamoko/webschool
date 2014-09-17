package models.reports;

import models.Utils;
import models.entities.FinalMark;
import models.entities.GosMark;
import models.entities.PracticMark;

/**
 * Класс для хранения элемента списка итоговых оценок в выписке.
 * В элементы именно этого класса будут преобразованы итоговые оценки, практики и т.п.
 * 
 * @author Воронин Леонид
 *
 */
public class MarkItem {
	public String subject = "";
	public String load = "";
	public String mark = "x";
	
	public MarkItem(final String subject, final String load, final String mark) {
		this.subject = subject;
		this.load = load;
		this.mark = mark;
	}
	
	public MarkItem(final String subject, final float load, final int mark) {
		this.subject = subject;
		this.load = Utils.getLenString(load);
		this.mark = Utils.getMarkString(mark);
	}
	
	public MarkItem(final String subject, final int load, final int mark) {
		this.subject = subject;
		this.load = load + "";
		this.mark = Utils.getMarkString(mark);
	}
	
	public MarkItem(final FinalMark mark) {
		this.subject = mark.getSubject();
		if (mark.maximumLoad > 0) {
			this.load = mark.maximumLoad + "";
		}
		this.mark = Utils.getMarkString(mark.mark);
	}
	
	public MarkItem(final PracticMark mark) {
		this.subject = mark.getPractic();
		this.load = Utils.getLenString(mark.length);
		this.mark = Utils.getMarkString(mark.mark);
	}

	public MarkItem(final GosMark mark) {
		this.subject = "Государственный экзамен (" + mark.getSubject() + ")";
		this.load = "x";
		this.mark = Utils.getMarkString(mark.mark);
	}
	
}
