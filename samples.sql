-- Создание таблицы book
CREATE TABLE IF NOT EXISTS book (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    year INT NOT NULL
);

-- Очистка таблицы
CREATE OR REPLACE PROCEDURE ClearTable()
LANGUAGE plpgsql
AS $$
BEGIN
DELETE FROM book;
END;
$$;

-- Добавление записи
CREATE OR REPLACE PROCEDURE AddBook(p_title VARCHAR(255), p_author VARCHAR(255), p_year INT)
LANGUAGE plpgsql
AS $$
BEGIN
INSERT INTO book (title, author, year) VALUES (p_title, p_author, p_year);
END;
$$;

-- Поиск книги по названию
CREATE OR REPLACE FUNCTION FindBookByTitle(p_title VARCHAR(255))
RETURNS TABLE(id INT, title VARCHAR(255), author VARCHAR(255), year INT)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY SELECT * FROM book WHERE book.title = p_title;
END;
$$;

-- Поиск книги по названию
CREATE OR REPLACE FUNCTION FindBookByAuthor(p_author VARCHAR(255))
RETURNS TABLE(id INT, title VARCHAR(255), author VARCHAR(255), year INT)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY SELECT * FROM book WHERE book.author = p_author;
END;
$$;

-- Поиск книги по названию
CREATE OR REPLACE FUNCTION FindBookByYear(p_year INT)
RETURNS TABLE(id INT, title VARCHAR(255), author VARCHAR(255), year INT)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY SELECT * FROM book WHERE book.year = p_year;
END;
$$;

-- Обновление книги
CREATE OR REPLACE PROCEDURE UpdateBook(p_id INT, p_title VARCHAR(255), p_author VARCHAR(255), p_year INT)
LANGUAGE plpgsql
AS $$
BEGIN
UPDATE book SET title = p_title, author = p_author, year = p_year WHERE id = p_id;
END;
$$;

-- Удаление книги по названию
CREATE OR REPLACE PROCEDURE DeleteBookByTitle(p_title VARCHAR(255))
LANGUAGE plpgsql
AS $$
BEGIN
DELETE FROM book WHERE book.title = p_title;
END;
$$;

-- Вывод всего содержимого таблицы
CREATE OR REPLACE FUNCTION ViewAllRecords()
RETURNS TABLE(id INT, title VARCHAR(255), author VARCHAR(255), year INT)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY SELECT * FROM book ORDER BY id;
END;
$$;

-- Создание роли администратора
CREATE ROLE admin WITH LOGIN PASSWORD 'admin_password';
ALTER ROLE admin WITH SUPERUSER; -- Администратор имеет все права

-- Создание роли модератора
CREATE ROLE moderator WITH LOGIN PASSWORD 'moderator_password';
GRANT ALL PRIVILEGES ON DATABASE library TO moderator; -- Модератор имеет все права на базу данных, кроме создания/удаления базы

-- Создание роли гостя
CREATE ROLE guest WITH LOGIN PASSWORD 'guest_password';
GRANT SELECT ON ALL TABLES IN SCHEMA public TO guest; -- Гость может только читать данные

-- Предоставление прав администратору
GRANT ALL PRIVILEGES ON DATABASE library TO admin; -- Полный доступ к базе данных
GRANT ALL PRIVILEGES ON TABLE book TO admin; -- Полный доступ к таблице book
GRANT EXECUTE ON PROCEDURE ClearTable() TO admin; -- Доступ к процедуре очистки таблицы
GRANT EXECUTE ON PROCEDURE AddBook(VARCHAR, VARCHAR, INT) TO admin; -- Доступ к процедуре добавления книги
GRANT EXECUTE ON FUNCTION FindBookByTitle(VARCHAR) TO admin; -- Доступ к функции поиска по названию
GRANT EXECUTE ON FUNCTION FindBookByAuthor(VARCHAR) TO admin; -- Доступ к функции поиска по автору
GRANT EXECUTE ON FUNCTION FindBookByYear(INT) TO admin; -- Доступ к функции поиска по году
GRANT EXECUTE ON PROCEDURE UpdateBook(INT, VARCHAR, VARCHAR, INT) TO admin; -- Доступ к процедуре обновления книги
GRANT EXECUTE ON PROCEDURE DeleteBookByTitle(VARCHAR) TO admin; -- Доступ к процедуре удаления книги по названию
GRANT EXECUTE ON FUNCTION ViewAllRecords() TO admin; -- Доступ к функции просмотра всех записей

-- Предоставление прав модератору
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE book TO moderator; -- Доступ к данным таблицы book
GRANT EXECUTE ON PROCEDURE ClearTable() TO moderator; -- Доступ к процедуре очистки таблицы
GRANT EXECUTE ON PROCEDURE AddBook(VARCHAR, VARCHAR, INT) TO moderator; -- Доступ к процедуре добавления книги
GRANT EXECUTE ON FUNCTION FindBookByTitle(VARCHAR) TO moderator; -- Доступ к функции поиска по названию
GRANT EXECUTE ON FUNCTION FindBookByAuthor(VARCHAR) TO moderator; -- Доступ к функции поиска по автору
GRANT EXECUTE ON FUNCTION FindBookByYear(INT) TO moderator; -- Доступ к функции поиска по году
GRANT EXECUTE ON PROCEDURE UpdateBook(INT, VARCHAR, VARCHAR, INT) TO moderator; -- Доступ к процедуре обновления книги
GRANT EXECUTE ON PROCEDURE DeleteBookByTitle(VARCHAR) TO moderator; -- Доступ к процедуре удаления книги по названию
GRANT EXECUTE ON FUNCTION ViewAllRecords() TO moderator; -- Доступ к функции просмотра всех записей

-- Предоставление прав гостю
GRANT SELECT ON TABLE book TO guest; -- Доступ только для чтения таблицы book
GRANT EXECUTE ON FUNCTION FindBookByTitle(VARCHAR) TO guest; -- Доступ к функции поиска по названию
GRANT EXECUTE ON FUNCTION FindBookByAuthor(VARCHAR) TO guest; -- Доступ к функции поиска по автору
GRANT EXECUTE ON FUNCTION FindBookByYear(INT) TO guest; -- Доступ к функции поиска по году
GRANT EXECUTE ON FUNCTION ViewAllRecords() TO guest; -- Доступ к функции просмотра всех записей