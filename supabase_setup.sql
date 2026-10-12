-- ==============================================================================
-- SCRIPT DE CONFIGURACIÓN DE BASE DE DATOS SUPABASE - PAN DE CASA
-- ==============================================================================
-- Este script crea todas las tablas, secuencias, triggers, políticas de seguridad
-- (RLS), el bucket de almacenamiento para imágenes y datos iniciales de prueba.
-- Ejecuta este script en: Supabase Dashboard -> SQL Editor -> New Query -> Run
-- ==============================================================================

-- 1. EXTENSIONES
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- 2. TABLA DE PRODUCTOS (products)
-- ==============================================================================
CREATE SEQUENCE IF NOT EXISTS products_id_seq;

CREATE TABLE IF NOT EXISTS public.products (
    id BIGINT DEFAULT nextval('products_id_seq') PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    image_url TEXT,
    category TEXT DEFAULT 'General',
    stock INTEGER DEFAULT 50,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER SEQUENCE products_id_seq OWNED BY public.products.id;

-- Trigger para garantizar que si la app envía 'id': null, se asigne automáticamente el siguiente ID
CREATE OR REPLACE FUNCTION set_product_id()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.id IS NULL THEN
        NEW.id := nextval('products_id_seq');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_set_product_id ON public.products;
CREATE TRIGGER trigger_set_product_id
BEFORE INSERT ON public.products
FOR EACH ROW
EXECUTE FUNCTION set_product_id();

-- ==============================================================================
-- 3. TABLA DE PEDIDOS (orders)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.orders (
    id TEXT PRIMARY KEY, -- Formato: ORD-123456 generado por la app
    customer_id TEXT NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    status TEXT NOT NULL DEFAULT 'RECIBIDO', -- RECIBIDO, PREPARANDO, LISTO, ENTREGADO
    created_at BIGINT NOT NULL, -- Unix timestamp en milisegundos desde la app
    customer_name TEXT NOT NULL,
    customer_address TEXT NOT NULL,
    customer_phone TEXT NOT NULL
);

-- ==============================================================================
-- 4. TABLA DE ÍTEMS DE PEDIDOS (order_items)
-- ==============================================================================
CREATE SEQUENCE IF NOT EXISTS order_items_id_seq;

CREATE TABLE IF NOT EXISTS public.order_items (
    id BIGINT DEFAULT nextval('order_items_id_seq') PRIMARY KEY,
    order_id TEXT NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1,
    price_at_time NUMERIC(10, 2) NOT NULL
);

ALTER SEQUENCE order_items_id_seq OWNED BY public.order_items.id;

-- Trigger para autoincremento seguro ante 'id': null
CREATE OR REPLACE FUNCTION set_order_item_id()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.id IS NULL THEN
        NEW.id := nextval('order_items_id_seq');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_set_order_item_id ON public.order_items;
CREATE TRIGGER trigger_set_order_item_id
BEFORE INSERT ON public.order_items
FOR EACH ROW
EXECUTE FUNCTION set_order_item_id();

-- ==============================================================================
-- 5. POLÍTICAS DE SEGURIDAD (Row Level Security - RLS)
-- ==============================================================================

-- Activar RLS en las tablas
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

-- Políticas para 'products':
-- Cualquiera (público o autenticado) puede leer los productos del catálogo
DROP POLICY IF EXISTS "Public products read access" ON public.products;
CREATE POLICY "Public products read access" 
ON public.products FOR SELECT 
USING (true);

-- Permite insertar, actualizar y eliminar productos (para Administrador y desarrollo)
DROP POLICY IF EXISTS "Public products insert access" ON public.products;
CREATE POLICY "Public products insert access" 
ON public.products FOR INSERT 
WITH CHECK (true);

DROP POLICY IF EXISTS "Public products update access" ON public.products;
CREATE POLICY "Public products update access" 
ON public.products FOR UPDATE 
USING (true);

DROP POLICY IF EXISTS "Public products delete access" ON public.products;
CREATE POLICY "Public products delete access" 
ON public.products FOR DELETE 
USING (true);

-- Políticas para 'orders':
-- Lectura de órdenes
DROP POLICY IF EXISTS "Public orders read access" ON public.orders;
CREATE POLICY "Public orders read access" 
ON public.orders FOR SELECT 
USING (true);

-- Creación de órdenes (clientes)
DROP POLICY IF EXISTS "Public orders insert access" ON public.orders;
CREATE POLICY "Public orders insert access" 
ON public.orders FOR INSERT 
WITH CHECK (true);

-- Actualización de estado de órdenes (cambio de status por admin)
DROP POLICY IF EXISTS "Public orders update access" ON public.orders;
CREATE POLICY "Public orders update access" 
ON public.orders FOR UPDATE 
USING (true);

-- Políticas para 'order_items':
DROP POLICY IF EXISTS "Public order_items read access" ON public.order_items;
CREATE POLICY "Public order_items read access" 
ON public.order_items FOR SELECT 
USING (true);

DROP POLICY IF EXISTS "Public order_items insert access" ON public.order_items;
CREATE POLICY "Public order_items insert access" 
ON public.order_items FOR INSERT 
WITH CHECK (true);

-- ==============================================================================
-- 6. BUCKET DE ALMACENAMIENTO PARA IMÁGENES (Storage: product-images)
-- ==============================================================================
-- Insertar el bucket público 'product-images' si no existe
INSERT INTO storage.buckets (id, name, public)
VALUES ('product-images', 'product-images', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Políticas de acceso para el bucket 'product-images':
DROP POLICY IF EXISTS "Public read product images" ON storage.objects;
CREATE POLICY "Public read product images" 
ON storage.objects FOR SELECT 
USING (bucket_id = 'product-images');

DROP POLICY IF EXISTS "Public upload product images" ON storage.objects;
CREATE POLICY "Public upload product images" 
ON storage.objects FOR INSERT 
WITH CHECK (bucket_id = 'product-images');

DROP POLICY IF EXISTS "Public update product images" ON storage.objects;
CREATE POLICY "Public update product images" 
ON storage.objects FOR UPDATE 
USING (bucket_id = 'product-images');

DROP POLICY IF EXISTS "Public delete product images" ON storage.objects;
CREATE POLICY "Public delete product images" 
ON storage.objects FOR DELETE 
USING (bucket_id = 'product-images');

-- ==============================================================================
-- 7. DATOS INICIALES DE PRUEBA (Catálogo Pan de Casa)
-- ==============================================================================
INSERT INTO public.products (name, description, price, image_url, category, stock)
VALUES 
(
    'Pan de Masa Madre Tradicional',
    'Pan rústico de fermentación lenta (24h) con corteza crujiente y miga aireada.',
    12000.00,
    'https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?auto=format&fit=crop&w=600&q=80',
    'Artesanal',
    25
),
(
    'Pan de Centeno y Semillas',
    'Elaborado con harina integral de centeno, semillas de girasol, lino y chía.',
    14000.00,
    'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80',
    'Integral',
    20
),
(
    'Croissant de Mantequilla',
    'Clásico croissant francés hojaldrado con mantequilla 100% natural de primera calidad.',
    6500.00,
    'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80',
    'Repostería',
    35
),
(
    'Baguette Rústica',
    'Baguette tradicional francesa, corteza dorada y crujiente, ideal para acompañar tus comidas.',
    5000.00,
    'https://images.unsplash.com/photo-1597079910443-60c43fc4f749?auto=format&fit=crop&w=600&q=80',
    'Artesanal',
    30
),
(
    'Pan de Queso y Ajo',
    'Pan suave horneado relleno con queso mozzarella fundido y mantequilla de ajo con finas hierbas.',
    8500.00,
    'https://images.unsplash.com/photo-1619535860434-ba1d8fa12536?auto=format&fit=crop&w=600&q=80',
    'Especialidades',
    15
),
(
    'Chocolatín (Pain au Chocolat)',
    'Hojaldre suave relleno de dos barras de chocolate belga semi-amargo.',
    7000.00,
    'https://images.unsplash.com/photo-1608198093002-ad4e005484ec?auto=format&fit=crop&w=600&q=80',
    'Repostería',
    25
)
ON CONFLICT DO NOTHING;
