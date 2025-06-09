//Trigger: Giảm tồn kho sau khi đặt hàng
CREATE OR REPLACE TRIGGER trg_decrease_book_stock
AFTER INSERT ON ORDER_ITEMS
FOR EACH ROW
BEGIN
  UPDATE BOOK
  SET QUANTITY = QUANTITY - :NEW.QUANTITY
  WHERE ID = :NEW.BOOK_ID;
END;
/

//Trigger: Kiểm tra tồn kho trước khi đặt hàng
CREATE OR REPLACE TRIGGER trg_check_stock_before_order
BEFORE INSERT ON ORDER_ITEMS
FOR EACH ROW
DECLARE
  v_stock NUMBER;
BEGIN
  SELECT QUANTITY INTO v_stock FROM BOOK WHERE ID = :NEW.BOOK_ID;
  IF v_stock < :NEW.QUANTITY THEN
    RAISE_APPLICATION_ERROR(-20001, 'Not enough stock for this book.');
  END IF;
END;
/

//Trigger cập nhật tồn kho khi hủy đơn hàng
CREATE OR REPLACE TRIGGER trg_restore_stock_on_cancel
AFTER UPDATE OF ORDER_STATUS ON ORDERS
FOR EACH ROW
WHEN (NEW.ORDER_STATUS = 'CANCELLED' AND OLD.ORDER_STATUS != 'CANCELLED')
DECLARE
BEGIN
  UPDATE BOOK b
  SET b.QUANTITY = b.QUANTITY + (
    SELECT oi.QUANTITY
    FROM ORDER_ITEMS oi
    WHERE oi.ORDER_ID = :NEW.ORDER_ID
      AND oi.BOOK_ID = b.ID
  )
  WHERE b.ID IN (
    SELECT BOOK_ID
    FROM ORDER_ITEMS
    WHERE ORDER_ID = :NEW.ORDER_ID
  );
END;
/

//Trigger kiểm tra phương thức thanh toán
CREATE OR REPLACE TRIGGER trg_validate_payment
BEFORE INSERT OR UPDATE ON PAYMENTS
FOR EACH ROW
BEGIN
  IF :NEW.PAYMENT_METHOD NOT IN ('COD', 'CARD', 'PAYPAL') THEN
    RAISE_APPLICATION_ERROR(-20002, 'Invalid payment method');
  END IF;
END;
/

//Trigger đồng bộ giá sách
CREATE OR REPLACE TRIGGER trg_sync_book_price
BEFORE INSERT ON ORDER_ITEMS
FOR EACH ROW
DECLARE
  v_current_price BOOK.PRICE%TYPE;
BEGIN
  SELECT PRICE INTO v_current_price
  FROM BOOK
  WHERE ID = :NEW.BOOK_ID;
  
  :NEW.ORDERED_BOOK_PRICE := v_current_price;
END;
/
----------------------------
SET SERVEROUTPUT ON;

//Procedure tìm các book có price trên 20
CREATE OR REPLACE PROCEDURE get_books_price_above_20 IS
BEGIN
  FOR book_rec IN (
    SELECT ID, TITLE, AUTHOR, PRICE
    FROM BOOK
    WHERE PRICE > 20
  ) LOOP
    DBMS_OUTPUT.PUT_LINE('ID: ' || book_rec.ID || 
                         ', Title: ' || book_rec.TITLE || 
                         ', Author: ' || book_rec.AUTHOR || 
                         ', Price: ' || book_rec.PRICE);
  END LOOP;
END;
/

//Procedure tìm các order có payment method = 'Card'
CREATE OR REPLACE PROCEDURE get_orders_by_card_payment IS
BEGIN
  FOR ord_rec IN (
    SELECT o.ORDER_ID, o.EMAIL, o.ORDER_DATE, o.TOTAL_AMOUNT, p.PAYMENT_METHOD
    FROM ORDERS o
    JOIN PAYMENTS p ON o.PAYMENT_ID = p.PAYMENT_ID
    WHERE p.PAYMENT_METHOD = 'Card'
  ) LOOP
    DBMS_OUTPUT.PUT_LINE('Order ID: ' || ord_rec.ORDER_ID ||
                         ', Email: ' || ord_rec.EMAIL ||
                         ', Date: ' || TO_CHAR(ord_rec.ORDER_DATE, 'YYYY-MM-DD') ||
                         ', Amount: ' || ord_rec.TOTAL_AMOUNT ||
                         ', Method: ' || ord_rec.PAYMENT_METHOD);
  END LOOP;
END;
/

//Procedure tìm những user có city ở 'DA NANG' hoặc 'HO CHI MINH' hoặc 'HA NOI'
CREATE OR REPLACE PROCEDURE get_users_by_city IS
BEGIN
  FOR user_rec IN (
    SELECT u.USER_ID, u.USER_NAME, u.EMAIL, a.CITY
    FROM APP_USER u
    JOIN ADDRESS a ON u.USER_ID = a.USER_ID
    WHERE a.CITY IN ('DA NANG', 'HO CHI MINH', 'HA NOI')
  ) LOOP
    DBMS_OUTPUT.PUT_LINE('User ID: ' || user_rec.USER_ID ||
                         ', Name: ' || user_rec.USER_NAME ||
                         ', Email: ' || user_rec.EMAIL ||
                         ', City: ' || user_rec.CITY);
  END LOOP;
END;
/

//Procedure cập nhật discount thành 10 cho những sách phát hành trước năm 2010 
CREATE OR REPLACE PROCEDURE update_old_books_discount IS
BEGIN
  UPDATE BOOK
  SET DISCOUNT = 10
  WHERE EXTRACT(YEAR FROM PUBLICATION_DATE) < 2010;

  COMMIT;
END;
/

//Procedure báo cáo doanh thu theo khoảng thời gian
CREATE OR REPLACE PROCEDURE GetSalesReport(
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_report OUT SYS_REFCURSOR
)
IS
BEGIN
    OPEN p_report FOR
    SELECT 
        TRUNC(o.order_date) AS sales_date,
        SUM(o.total_amount) AS daily_sales,
        COUNT(*) AS order_count,
        SUM(oi.quantity) AS total_books_sold
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.order_id
    WHERE o.order_date BETWEEN p_start_date AND p_end_date
      AND o.order_status = 'Order Accepted !'
    GROUP BY TRUNC(o.order_date)
    ORDER BY sales_date;
END;
/
DECLARE
  v_cursor SYS_REFCURSOR;
  v_sales_date    DATE;
  v_daily_sales   NUMBER;
  v_order_count   NUMBER;
  v_books_sold    NUMBER;
BEGIN
  GetSalesReport(
    p_start_date => TO_DATE('2024-01-01', 'YYYY-MM-DD'),
    p_end_date   => TO_DATE('2024-12-31', 'YYYY-MM-DD'),
    p_report     => v_cursor
  );

  LOOP
    FETCH v_cursor INTO v_sales_date, v_daily_sales, v_order_count, v_books_sold;
    EXIT WHEN v_cursor%NOTFOUND;

    DBMS_OUTPUT.PUT_LINE(
      'Ngày: ' || TO_CHAR(v_sales_date, 'YYYY-MM-DD') ||
      ' | Doanh thu: ' || v_daily_sales ||
      ' | Số đơn: ' || v_order_count ||
      ' | Tổng sách bán: ' || v_books_sold
    );
  END LOOP;

  CLOSE v_cursor;
END;