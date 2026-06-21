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

type ApiError = {
  message?: string
}

const emptyForm: ProductForm = {
  name: '',
  description: '',
  category: '',
  price: '',
}

const currencyFormatter = new Intl.NumberFormat('it-IT', {
  style: 'currency',
  currency: 'EUR',
})

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

function toForm(product: Product): ProductForm {
  return {
    name: product.name,
    description: product.description,
    category: product.category,
    price: String(product.price),
  }
}

function toPayload(form: ProductForm) {
  return {
    name: form.name.trim(),
    description: form.description.trim(),
    category: form.category.trim(),
    price: Number(form.price),
  }
}

function App() {
  const [products, setProducts] = useState<Product[]>([])
  const [form, setForm] = useState<ProductForm>(emptyForm)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const totalValue = useMemo(
    () => products.reduce((sum, product) => sum + Number(product.price), 0),
    [products],
  )

  async function loadProducts() {
    setIsLoading(true)
    setError(null)

    try {
      const data = await request<Product[]>('/products')
      setProducts(data)
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    let isMounted = true

    async function loadInitialProducts() {
      try {
        const data = await request<Product[]>('/products')

        if (isMounted) {
          setProducts(data)
        }
      } catch (exception) {
        if (isMounted) {
          setError(exception instanceof Error ? exception.message : 'Errore inatteso')
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    void loadInitialProducts()

    return () => {
      isMounted = false
    }
  }, [])

  function handleChange(field: keyof ProductForm, value: string) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  function startCreate() {
    setSelectedProduct(null)
    setForm(emptyForm)
    setError(null)
  }

  function startEdit(product: Product) {
    setSelectedProduct(product)
    setForm(toForm(product))
    setError(null)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSaving(true)
    setError(null)

    try {
      const payload = toPayload(form)

      if (selectedProduct) {
        await request<Product>(`/products/${selectedProduct.id}`, {
          method: 'PUT',
          body: JSON.stringify(payload),
        })
      } else {
        await request<Product>('/products', {
          method: 'POST',
          body: JSON.stringify(payload),
        })
      }

      setForm(emptyForm)
      setSelectedProduct(null)
      await loadProducts()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(product: Product) {
    const confirmed = window.confirm(`Eliminare "${product.name}"?`)

    if (!confirmed) {
      return
    }

    setError(null)

    try {
      await request<void>(`/products/${product.id}`, { method: 'DELETE' })

      if (selectedProduct?.id === product.id) {
        startCreate()
      }

      await loadProducts()
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Errore inatteso')
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">ET</span>
          <div>
            <strong>EuroTransit</strong>
            <span>Catalog Console</span>
          </div>
        </div>
        <nav className="nav-list" aria-label="Sezioni">
          <a className="active" href="#products">Products</a>
          <a href="#inventory">Inventory</a>
          <a href="#settings">Settings</a>
        </nav>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Catalog</p>
            <h1>Products</h1>
          </div>
          <button type="button" className="secondary-button" onClick={loadProducts}>
            Refresh
          </button>
        </header>

        {error && (
          <div className="alert" role="alert">
            {error}
          </div>
        )}

        <section className="summary-grid" aria-label="Riepilogo prodotti">
          <article>
            <span>Records</span>
            <strong>{products.length}</strong>
          </article>
          <article>
            <span>Total value</span>
            <strong>{currencyFormatter.format(totalValue)}</strong>
          </article>
          <article>
            <span>Status</span>
            <strong>{isLoading ? 'Syncing' : 'Ready'}</strong>
          </article>
        </section>

        <div className="content-grid" id="products">
          <section className="panel">
            <div className="panel-header">
              <div>
                <h2>Product list</h2>
                <p>Products exposed by the catalog backend.</p>
              </div>
              <button type="button" className="primary-button" onClick={startCreate}>
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
                  {isLoading ? (
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
                            onClick={() => startEdit(product)}
                          >
                            {product.name}
                          </button>
                          <span>{product.description}</span>
                        </td>
                        <td>{product.category}</td>
                        <td className="numeric">{currencyFormatter.format(product.price)}</td>
                        <td className="actions">
                          <button type="button" onClick={() => startEdit(product)}>
                            Edit
                          </button>
                          <button
                            type="button"
                            className="danger"
                            onClick={() => void handleDelete(product)}
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

            <form onSubmit={(event) => void handleSubmit(event)}>
              <label>
                Name
                <input
                  required
                  value={form.name}
                  onChange={(event) => handleChange('name', event.target.value)}
                />
              </label>
              <label>
                Description
                <textarea
                  required
                  rows={4}
                  value={form.description}
                  onChange={(event) => handleChange('description', event.target.value)}
                />
              </label>
              <label>
                Category
                <input
                  required
                  value={form.category}
                  onChange={(event) => handleChange('category', event.target.value)}
                />
              </label>
              <label>
                Price
                <input
                  required
                  min="0"
                  step="0.01"
                  type="number"
                  value={form.price}
                  onChange={(event) => handleChange('price', event.target.value)}
                />
              </label>
              <div className="form-actions">
                <button type="submit" className="primary-button" disabled={isSaving}>
                  {isSaving ? 'Saving...' : selectedProduct ? 'Save changes' : 'Create'}
                </button>
                <button type="button" className="secondary-button" onClick={startCreate}>
                  Reset
                </button>
              </div>
            </form>
          </section>
        </div>
      </main>
    </div>
  )
}

export default App
