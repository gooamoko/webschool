package models;

/**
 * Класс для реализации исключения при формировании отчета.
 * 
 * @author Воронин Леонид
 *
 */
public class ModelException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ModelException(String message) {
		super("Исключение ModelException. " + message);
	}
}
