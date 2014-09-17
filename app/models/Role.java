package models;

/**
 * Перечисление для задания ролей
 *
 * @author Воронин Леонид
 */
public enum Role {

  USER, ADMIN, DEPARTMENT, METHODIST, RECEPTION;

  public String getDescription() {
    switch (this) {
      case USER:
        return "Пользователь";
      case ADMIN:
        return "Администратор";
      case DEPARTMENT:
          return "Учебное отделение";
      case METHODIST:
          return "Учебная часть";
      case RECEPTION:
          return "Приемная комиссия";
    }
    return "Неизвестная роль";
  }
}
