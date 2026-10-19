# Lista de Productos — IDs para Códigos QR

Estos son los productos precargados en la base de datos. El contenido del código QR puede ser el **ID numérico** o el **Código Interno**.

| ID | Código Interno | Nombre | Categoría | Stock Inicial | Precio Venta |
|----|----------------|--------|-----------|---------------|--------------|
| 1  | FIL-001        | Filtro de aceite genérico | Filtros | 15 | $6.500 |
| 2  | FIL-002        | Filtro de aire estándar | Filtros | 8 | $7.800 |
| 3  | ACE-001        | Aceite motor 5W-30 sintético | Aceites | 20 | $9.500 |
| 4  | ACE-002        | Aceite motor 10W-40 semisintético | Aceites | 12 | $7.500 |
| 5  | FRE-001        | Pastillas de freno delanteras | Frenos | 6 | $22.000 |
| 6  | FRE-002        | Disco de freno ventilado | Frenos | 3 | $32.000 |
| 7  | ELE-001        | Batería 12V 60Ah | Eléctrico | 4 | $72.000 |
| 8  | ELE-002        | Bujía de encendido estándar | Eléctrico | 25 | $3.500 |
| 9  | SUS-001        | Amortiguador delantero | Suspensión | 2 | $48.000 |
| 10 | REF-001        | Refrigerante motor verde | Refrigerantes | 10 | $5.800 |

---

## Contenido válido para un código QR

El escáner acepta cualquiera de estos formatos:

- **ID directo**: `1`, `2`, `3`, ..., `10`
- **Con prefijo**: `ID: 5`, `id=5`, `producto: 7`
- **Código interno**: `FIL-001`, `ACE-001`, `FRE-002`, etc.
