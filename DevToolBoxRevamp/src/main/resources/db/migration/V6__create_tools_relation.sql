CREATE TABLE tools(
id INT NOT NULL,
sub_id INT NOT NULL,
category VARCHAR NOT NULL,
tool_category  VARCHAR(100) NOT NULL,
tool_sub_category VARCHAR(100) NOT NULL,
is_active boolean DEFAULT TRUE NOT NULL,

CONSTRAINT composite_pk_tools_id_sub_id PRIMARY KEY(id,sub_id)
);

INSERT INTO tools (
    id,
    sub_id,
    category,
    tool_category,
    tool_sub_category,
    is_active
)
VALUES
    -- Image Tools
    (1, 1, 'USER', 'Image Tools', 'Image Compressor', TRUE),
    (1, 2, 'USER', 'Image Tools', 'Image Converter', TRUE),

    -- Productivity Tools
    (2, 1, 'USER', 'Productivity Tools', 'QR Code Generator', TRUE),
    (2, 2, 'USER', 'Productivity Tools', 'UUID Generator', TRUE),
    (2, 3, 'USER', 'Productivity Tools', 'Word Counter', TRUE),

    -- PDF Tools
    (3, 1, 'USER', 'PDF Tools', 'PDF Merger', TRUE),
    (3, 2, 'USER', 'PDF Tools', 'PDF Splitter', TRUE),
    (3, 3, 'USER', 'PDF Tools', 'PDF Compressor', TRUE),
    (3, 4, 'USER', 'PDF Tools', 'PDF to Image', TRUE),
    (3, 5, 'USER', 'PDF Tools', 'Image to PDF', TRUE);



