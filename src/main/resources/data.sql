INSERT INTO permissions (name, description) VALUES
('MANAGE_USERS',   'Crear, editar y eliminar usuarios'),     
('MANAGE_ROLES',   'Crear, editar y eliminar roles'),        
('READ_PRODUCT',   'Consultar productos'),                   
('UPDATE_PRODUCT', 'Actualizar productos'),                  
('CREATE_PRODUCT', 'Crear productos'),                       
('DELETE_PRODUCT', 'Eliminar productos'),                    
('CREATE_ORDER',   'Crear pedidos'),                         
('READ_ORDER',     'Consultar pedidos');                     

INSERT INTO roles (name, description) VALUES
('ADMIN',    'Administrador del sistema'),                   
('SELLER',   'Vendedor que gestiona el catalogo'),           
('CUSTOMER', 'Cliente que realiza compras');                 

INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),  
(2, 3), (2, 4), (2, 5), (2, 8),                                  
(3, 4), (3, 7), (3, 8);                                          

INSERT INTO users (username, email, password, full_name) VALUES
('admin',     'admin@tienda.com',     'admin123',  'Administrador General'),  
('vendedor1', 'vendedor1@tienda.com', 'vend123',   'Laura Gomez'),            
('cliente1',  'cliente1@tienda.com',  'cli123',    'Carlos Perez'),           
('cliente2',  'cliente2@tienda.com',  'cli456',    'Maria Rodriguez'),        
('mixto1',    'mixto1@tienda.com',    'mix123',    'Andres Lopez');           

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),          
(2, 2),         
(3, 3),          
(4, 3),          
(5, 2), (5, 3);  

INSERT INTO categories (name, description) VALUES
('Electronica', 'Dispositivos y accesorios electronicos'),  
('Hogar',       'Articulos para el hogar'),                 
('Ropa',        'Prendas de vestir'),                       
('Libros',      'Libros impresos');                         

INSERT INTO products (name, description, price, stock, category_id) VALUES
('Laptop Lenovo',        'Laptop 16GB RAM 512GB SSD', 2500000.00,  10, 1),  
('Audifonos Bluetooth',  'Audifonos inalambricos',     180000.00,  50, 1),  
('Mouse inalambrico',    'Mouse optico USB',            60000.00, 100, 1),  
('Licuadora',            'Licuadora 10 velocidades',   220000.00,  20, 2),  
('Juego de sartenes',    'Set de 3 sartenes',          150000.00,  15, 2),  
('Camiseta algodon',     'Camiseta basica talla M',     45000.00,  80, 3),  
('Chaqueta impermeable', 'Chaqueta para lluvia',       210000.00,  25, 3),  
('Clean Code',           'Libro de Robert C. Martin',  120000.00,  30, 4);  

INSERT INTO orders (order_date, status, total, user_id) VALUES
('2026-09-01 10:30:00', 'PAID',    2620000.00, 3),  
('2026-09-10 15:00:00', 'SHIPPED',  355000.00, 4),  
('2026-09-20 09:15:00', 'PENDING',  300000.00, 3);  

INSERT INTO order_items (quantity, unit_price, order_id, product_id) VALUES
(1, 2500000.00, 1, 1),  
(2,   60000.00, 1, 3),  
(1,  220000.00, 2, 4),  
(3,   45000.00, 2, 6),  
(1,  180000.00, 3, 2),  
(1,  120000.00, 3, 8);  