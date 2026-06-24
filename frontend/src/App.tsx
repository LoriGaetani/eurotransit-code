import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Product = {
  id: number
  name: string
  description: string
  category: string
  price: number
}

type ProductForm = {
  name: string
  description: string
  category: string
  price: string
}

type InventoryItem = {
  productId: number
  quantity: number
  version: number
}

type InventoryForm = {
  productId: string
  quantity: string
}

type Section = 'products' | 'inventory'

type ApiError = {
  message?: string
}

const emptyProductForm: ProductForm = {
  name: '',
  description: '',
  category: '',
  price: '',
}

const emptyInventoryForm: InventoryForm = {
  productId: '',
  quantity: '',
}

const currencyFormatter = new Intl.NumberFormat('it-IT', {
  style: 'currency',
  currency: 'EUR',
})

const productsPath = '/catalog/products'
const inventoryItemsPath = '/inventory/items'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
    ...init,
  })

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`

    try {
      const body = (await response.json()) as ApiError
      message = body.message || message
    } catch {
      const text = await response.text()
      message = text || message
    }

    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

function sectionFromHash(): Section {
  return window.location.hash === '#inventory' ? 'inventory' : 'products'
}

function toProductForm(product: Product): ProductForm {
  return {
    name: product.name,
    description: product.description,
    category: product.category,
    price: String(product.price),
  }
}

function toProductPayload(form: ProductForm) {
  return {
    name: form.name.trim(),
    description: form.description.trim(),
    category: form.category.trim(),
    price: Number(form.price),
  }
}

function toInventoryForm(inventoryItem: InventoryItem): InventoryForm {
  return {
    productId: String(inventoryItem.productId),
    quantity: String(inventoryItem.quantity),
  }
}

function toInventoryPayload(form: InventoryForm) {
  return {
    productId: Number(form.productId),
    quantity: Number(form.quantity),
  }
}

function App() {
  const [activeSection, setActiveSection] = useState<Section>(sectionFromHash)
  const [products, setProducts] = useState<Product[]>([])
  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([])
  const [productForm, setProductForm] = useState<ProductForm>(emptyProductForm)
  const [inventoryForm, setInventoryForm] = useState<InventoryForm>(emptyInventoryForm)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [selectedInventoryItem, setSelectedInventoryItem] = useState<InventoryItem | null>(null)
  const [isProductsLoading, setIsProductsLoading] = useState(true)
  const [isInventoryLoading, setIsInventoryLoading] = useState(true)
  const [isSavingProduct, setIsSavingProduct] = useState(false)
  const [isSavingInventory, setIsSavingInventory] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const productsById = useMemo(
    () => new Map(products.map((product) => [product.id, product])),
    [products],
  )

  const productTotalValue = useMemo(
    () => products.reduce((sum, product) => sum + Number(product.price), 0),
    [products],
  )

  const inventoryUnits = useMemo(
    () => inventoryItems.reduce((sum, inventoryItem) => sum + inventoryItem.quantity, 0),
    [inventoryItems],
  )

  const inventoryValue = useMemo(
    () =>
      inventoryItems.reduce((sum, inventoryItem) => {
        const product = productsById.get(inventoryItem.productId)
        return sum + inventoryItem.quantity * Number(product?.price || 0)
      }, 0),
    [inventoryItems, productsById],
  )

  const productsWithoutInventory = useMemo(() => {
    const stockedProductIds = new Set(inventoryItems.map((inventoryItem) => inventoryItem.productId))
    return products.filter((product) => !stockedProductIds.has(product.id))
  }, [inventoryItems, products])

  async function loadProducts() {
    setIsProductsLoading(true)
    setError(null)

    try {
      const data = await request<Product[]>(productsPath)
      setProducts(data)
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    } finally {
      setIsProductsLoading(false)
    }
  }

  async function loadInventory() {
    setIsInventoryLoading(true)
    setError(null)

    try {
      const [productData, inventoryData] = await Promise.all([
        request<Product[]>(productsPath),
        request<InventoryItem[]>(inventoryItemsPath),
      ])
      setProducts(productData)
      setInventoryItems(inventoryData)
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    } finally {
      setIsInventoryLoading(false)
    }
  }

  async function loadAll() {
    setIsProductsLoading(true)
    setIsInventoryLoading(true)
    setError(null)

    try {
      const [productData, inventoryData] = await Promise.all([
        request<Product[]>(productsPath),
        request<InventoryItem[]>(inventoryItemsPath),
      ])
      setProducts(productData)
      setInventoryItems(inventoryData)
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    } finally {
      setIsProductsLoading(false)
      setIsInventoryLoading(false)
    }
  }

  useEffect(() => {
    let isMounted = true

    async function loadInitialData() {
      try {
        const [productData, inventoryData] = await Promise.all([
          request<Product[]>(productsPath),
          request<InventoryItem[]>(inventoryItemsPath),
        ])

        if (isMounted) {
          setProducts(productData)
          setInventoryItems(inventoryData)
        }
      } catch (exception) {
        if (isMounted) {
          setError(exception instanceof Error ? exception.message : 'Errore inatteso')
        }
      } finally {
        if (isMounted) {
          setIsProductsLoading(false)
          setIsInventoryLoading(false)
        }
      }
    }

    void loadInitialData()

    return () => {
      isMounted = false
    }
  }, [])

  useEffect(() => {
    function syncSectionFromHash() {
      setActiveSection(sectionFromHash())
    }

    window.addEventListener('hashchange', syncSectionFromHash)

    return () => {
      window.removeEventListener('hashchange', syncSectionFromHash)
    }
  }, [])

  function changeSection(section: Section) {
    setActiveSection(section)
    window.history.replaceState(null, '', `#${section}`)
  }

  function handleProductChange(field: keyof ProductForm, value: string) {
    setProductForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  function handleInventoryChange(field: keyof InventoryForm, value: string) {
    setInventoryForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  function startCreateProduct() {
    setSelectedProduct(null)
    setProductForm(emptyProductForm)
    setError(null)
  }

  function startEditProduct(product: Product) {
    setSelectedProduct(product)
    setProductForm(toProductForm(product))
    setError(null)
  }

  function startCreateInventoryItem(productId?: number) {
    setSelectedInventoryItem(null)
    setInventoryForm({
      ...emptyInventoryForm,
      productId: productId ? String(productId) : '',
    })
    setError(null)
  }

  function startEditInventoryItem(inventoryItem: InventoryItem) {
    setSelectedInventoryItem(inventoryItem)
    setInventoryForm(toInventoryForm(inventoryItem))
    setError(null)
  }

  async function handleProductSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSavingProduct(true)
    setError(null)

    try {
      const payload = toProductPayload(productForm)

      if (selectedProduct) {
        await request<Product>(`${productsPath}/${selectedProduct.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload),
        })
      } else {
        await request<Product>(productsPath, {
          method: 'POST',
          body: JSON.stringify(payload),
        })
      }

      setProductForm(emptyProductForm)
      setSelectedProduct(null)
      await loadProducts()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    } finally {
      setIsSavingProduct(false)
    }
  }

  async function handleInventorySubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSavingInventory(true)
    setError(null)

    try {
      const payload = toInventoryPayload(inventoryForm)

      if (selectedInventoryItem) {
        await request<InventoryItem>(`${inventoryItemsPath}/${selectedInventoryItem.productId}`, {
          method: 'PUT',
          body: JSON.stringify(payload),
        })
      } else {
        await request<InventoryItem>(inventoryItemsPath, {
          method: 'POST',
          body: JSON.stringify(payload),
        })
      }

      setInventoryForm(emptyInventoryForm)
      setSelectedInventoryItem(null)
      await loadInventory()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    } finally {
      setIsSavingInventory(false)
    }
  }

  async function handleProductDelete(product: Product) {
    const confirmed = window.confirm(`Eliminare "${product.name}"?`)

    if (!confirmed) {
      return
    }

    setError(null)

    try {
      await request<void>(`${productsPath}/${product.id}`, { method: 'DELETE' })

      if (selectedProduct?.id === product.id) {
        startCreateProduct()
      }

      await loadAll()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    }
  }

  async function handleInventoryDelete(inventoryItem: InventoryItem) {
    const product = productsById.get(inventoryItem.productId)
    const confirmed = window.confirm(`Eliminare lo stock per "${product?.name || inventoryItem.productId}"?`)

    if (!confirmed) {
      return
    }

    setError(null)

    try {
      await request<void>(`${inventoryItemsPath}/${inventoryItem.productId}`, { method: 'DELETE' })

      if (selectedInventoryItem?.productId === inventoryItem.productId) {
        startCreateInventoryItem()
      }

      await loadInventory()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    }
  }

  const isProductsView = activeSection === 'products'
  const isInventoryView = activeSection === 'inventory'

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">ET</span>
          <div>
            <strong>EuroTransit</strong>
            <span>Operations Console</span>
          </div>
        </div>
        <nav className="nav-list" aria-label="Sezioni">
          <a
            className={isProductsView ? 'active' : undefined}
            href="#products"
            onClick={() => changeSection('products')}
          >
            Products
          </a>
          <a
            className={isInventoryView ? 'active' : undefined}
            href="#inventory"
            onClick={() => changeSection('inventory')}
          >
            Inventory
          </a>
          <a href="#settings">Settings</a>
        </nav>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">{isProductsView ? 'Catalog' : 'Inventory'}</p>
            <h1>{isProductsView ? 'Products' : 'Stock'}</h1>
          </div>
          <button
            type="button"
            className="secondary-button"
            onClick={() => void (isProductsView ? loadProducts() : loadInventory())}
          >
            Refresh
          </button>
        </header>

        {error && (
          <div className="alert" role="alert">
            {error}
          </div>
        )}

        {isProductsView ? (
          <>
            <section className="summary-grid" aria-label="Riepilogo prodotti">
              <article>
                <span>Records</span>
                <strong>{products.length}</strong>
              </article>
              <article>
                <span>Total value</span>
                <strong>{currencyFormatter.format(productTotalValue)}</strong>
              </article>
              <article>
                <span>Status</span>
                <strong>{isProductsLoading ? 'Syncing' : 'Ready'}</strong>
              </article>
            </section>

            <div className="content-grid" id="products">
              <section className="panel">
                <div className="panel-header">
                  <div>
                    <h2>Product list</h2>
                    <p>Products exposed by the catalog backend.</p>
                  </div>
                  <button type="button" className="primary-button" onClick={startCreateProduct}>
                    New product
                  </button>
                </div>

                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Category</th>
                        <th className="numeric">Price</th>
                        <th className="actions">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {isProductsLoading ? (
                        <tr>
                          <td colSpan={4} className="empty-state">Loading products...</td>
                        </tr>
                      ) : products.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="empty-state">No products found.</td>
                        </tr>
                      ) : (
                        products.map((product) => (
                          <tr key={product.id}>
                            <td>
                              <button
                                type="button"
                                className="row-title"
                                onClick={() => startEditProduct(product)}
                              >
                                {product.name}
                              </button>
                              <span>{product.description}</span>
                            </td>
                            <td>{product.category}</td>
                            <td className="numeric">{currencyFormatter.format(product.price)}</td>
                            <td className="actions">
                              <button type="button" onClick={() => startEditProduct(product)}>
                                Edit
                              </button>
                              <button
                                type="button"
                                className="danger"
                                onClick={() => void handleProductDelete(product)}
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </section>

              <section className="panel form-panel">
                <div className="panel-header">
                  <div>
                    <h2>{selectedProduct ? 'Edit product' : 'Create product'}</h2>
                    <p>{selectedProduct ? `ID ${selectedProduct.id}` : 'Add a catalog record.'}</p>
                  </div>
                </div>

                <form onSubmit={(event) => void handleProductSubmit(event)}>
                  <label>
                    Name
                    <input
                      required
                      value={productForm.name}
                      onChange={(event) => handleProductChange('name', event.target.value)}
                    />
                  </label>
                  <label>
                    Description
                    <textarea
                      required
                      rows={4}
                      value={productForm.description}
                      onChange={(event) => handleProductChange('description', event.target.value)}
                    />
                  </label>
                  <label>
                    Category
                    <input
                      required
                      value={productForm.category}
                      onChange={(event) => handleProductChange('category', event.target.value)}
                    />
                  </label>
                  <label>
                    Price
                    <input
                      required
                      min="0"
                      step="0.01"
                      type="number"
                      value={productForm.price}
                      onChange={(event) => handleProductChange('price', event.target.value)}
                    />
                  </label>
                  <div className="form-actions">
                    <button type="submit" className="primary-button" disabled={isSavingProduct}>
                      {isSavingProduct ? 'Saving...' : selectedProduct ? 'Save changes' : 'Create'}
                    </button>
                    <button type="button" className="secondary-button" onClick={startCreateProduct}>
                      Reset
                    </button>
                  </div>
                </form>
              </section>
            </div>
          </>
        ) : (
          <>
            <section className="summary-grid" aria-label="Riepilogo inventario">
              <article>
                <span>Stock records</span>
                <strong>{inventoryItems.length}</strong>
              </article>
              <article>
                <span>Units on hand</span>
                <strong>{inventoryUnits}</strong>
              </article>
              <article>
                <span>Stock value</span>
                <strong>{currencyFormatter.format(inventoryValue)}</strong>
              </article>
            </section>

            <div className="content-grid" id="inventory">
              <section className="panel">
                <div className="panel-header">
                  <div>
                    <h2>Inventory list</h2>
                    <p>Stock records backed by the inventory service.</p>
                  </div>
                  <button type="button" className="primary-button" onClick={() => startCreateInventoryItem()}>
                    New stock
                  </button>
                </div>

                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Product</th>
                        <th className="numeric">Quantity</th>
                        <th className="numeric">Version</th>
                        <th className="actions">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {isInventoryLoading ? (
                        <tr>
                          <td colSpan={4} className="empty-state">Loading inventory...</td>
                        </tr>
                      ) : inventoryItems.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="empty-state">No inventory found.</td>
                        </tr>
                      ) : (
                        inventoryItems.map((inventoryItem) => {
                          const product = productsById.get(inventoryItem.productId)

                          return (
                            <tr key={inventoryItem.productId}>
                              <td>
                                <button
                                  type="button"
                                  className="row-title"
                                  onClick={() => startEditInventoryItem(inventoryItem)}
                                >
                                  {product?.name || `Product ${inventoryItem.productId}`}
                                </button>
                                <span>
                                  ID {inventoryItem.productId}
                                  {product ? ` · ${product.category}` : ' · missing catalog record'}
                                </span>
                              </td>
                              <td className="numeric">{inventoryItem.quantity}</td>
                              <td className="numeric">{inventoryItem.version}</td>
                              <td className="actions">
                                <button type="button" onClick={() => startEditInventoryItem(inventoryItem)}>
                                  Edit
                                </button>
                                <button
                                  type="button"
                                  className="danger"
                                  onClick={() => void handleInventoryDelete(inventoryItem)}
                                >
                                  Delete
                                </button>
                              </td>
                            </tr>
                          )
                        })
                      )}
                    </tbody>
                  </table>
                </div>
              </section>

              <section className="panel form-panel">
                <div className="panel-header">
                  <div>
                    <h2>{selectedInventoryItem ? 'Edit stock' : 'Create stock'}</h2>
                    <p>
                      {selectedInventoryItem
                        ? `Product ID ${selectedInventoryItem.productId}`
                        : 'Use an existing catalog product.'}
                    </p>
                  </div>
                </div>

                <form onSubmit={(event) => void handleInventorySubmit(event)}>
                  <label>
                    Product
                    <select
                      required
                      disabled={Boolean(selectedInventoryItem)}
                      value={inventoryForm.productId}
                      onChange={(event) => handleInventoryChange('productId', event.target.value)}
                    >
                      <option value="">Select product</option>
                      {(selectedInventoryItem
                        ? products.filter((product) => product.id === selectedInventoryItem.productId)
                        : productsWithoutInventory
                      ).map((product) => (
                        <option key={product.id} value={product.id}>
                          {product.name} · ID {product.id}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Quantity
                    <input
                      required
                      min="0"
                      step="1"
                      type="number"
                      value={inventoryForm.quantity}
                      onChange={(event) => handleInventoryChange('quantity', event.target.value)}
                    />
                  </label>
                  <div className="form-actions">
                    <button type="submit" className="primary-button" disabled={isSavingInventory}>
                      {isSavingInventory ? 'Saving...' : selectedInventoryItem ? 'Save changes' : 'Create'}
                    </button>
                    <button type="button" className="secondary-button" onClick={() => startCreateInventoryItem()}>
                      Reset
                    </button>
                  </div>
                </form>

                {!selectedInventoryItem && productsWithoutInventory.length > 0 && (
                  <div className="quick-picks">
                    <span>Without stock</span>
                    {productsWithoutInventory.slice(0, 5).map((product) => (
                      <button
                        key={product.id}
                        type="button"
                        onClick={() => startCreateInventoryItem(product.id)}
                      >
                        {product.name}
                      </button>
                    ))}
                  </div>
                )}
              </section>
            </div>
          </>
        )}
      </main>
    </div>
  )
}

export default App
