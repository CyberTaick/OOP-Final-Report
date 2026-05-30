//javascript connect to java, replace "main.js" with this file if connected to java background

const API_BASE_URL = "http://localhost:8080/api";

const state = {
  products: [],
  cart: [],
  searchTerm: "",
  activeCategory: "所有",
  discountCode: "",
  shippingMethod: "flat",
  paymentMethod: "card",
  paymentAccount: "",
  totals: { subtotal: 0, discount: 0, shipping: 0, total: 0 },
  user: null,
  adminTab: "products",
  adminEditingProduct: null,
};

const dom = {
  products: document.getElementById("products"),
  categoryList: document.getElementById("categoryList"),
  searchInput: document.getElementById("searchInput"),
  searchBtn: document.getElementById("searchBtn"),
  cart: document.getElementById("cart"),
  subtotal: document.getElementById("subtotal"),
  shipping: document.getElementById("shipping"),
  discount: document.getElementById("discount"),
  total: document.getElementById("total"),
  checkoutBtn: document.getElementById("checkoutBtn"),
  message: document.getElementById("message"),
  productModal: document.getElementById("productModal"),
  closeProductModal: document.getElementById("closeProductModal"),
  modalProductName: document.getElementById("modalProductName"),
  modalProductDesc: document.getElementById("modalProductDesc"),
  modalProductPrice: document.getElementById("modalProductPrice"),
  modalProductCategory: document.getElementById("modalProductCategory"),
  modalProductStock: document.getElementById("modalProductStock"),
  modalAddBtn: document.getElementById("modalAddBtn"),
  paymentModal: document.getElementById("paymentModal"),
  closePaymentModal: document.getElementById("closePaymentModal"),
  paymentMethod: document.getElementById("paymentMethod"),
  checkoutCard: document.getElementById("checkoutCard"),
  discountCode: document.getElementById("discountCode"),
  paymentMessage: document.getElementById("paymentMessage"),
  confirmPaymentBtn: document.getElementById("confirmPaymentBtn"),
  shippingSelect: document.getElementById("shippingSelect"),
  openLoginBtn: document.getElementById("openLoginBtn"),
  closeLoginModal: document.getElementById("closeLoginModal"),
  loginModal: document.getElementById("loginModal"),
  loginUsername: document.getElementById("loginUsername"),
  loginPassword: document.getElementById("loginPassword"),
  loginRole: document.getElementById("loginRole"),
  loginBtn: document.getElementById("loginBtn"),
  loginModalMessage: document.getElementById("loginModalMessage"),
  logoutBtn: document.getElementById("logoutBtn"),
  currentUser: document.getElementById("currentUser"),
  displayName: document.getElementById("displayName"),
  manageProductsBtn: document.getElementById("manageProductsBtn"),
  checkoutUserInfo: document.getElementById("checkoutUserInfo"),
  checkoutName: document.getElementById("checkoutName"),
  adminPanel: document.getElementById("adminPanel"),
  adminTabs: document.querySelectorAll("#adminPanel .admin-tabs button"),
  adminContent: document.getElementById("adminContent"),
  adminEditModal: document.getElementById("adminEditModal"),
  closeAdminEditModal: document.getElementById("closeAdminEditModal"),
  adminModalName: document.getElementById("adminModalName"),
  adminModalCategory: document.getElementById("adminModalCategory"),
  adminModalPrice: document.getElementById("adminModalPrice"),
  adminModalStock: document.getElementById("adminModalStock"),
  adminModalImages: document.getElementById("adminModalImages"),
  adminModalCancelBtn: document.getElementById("adminModalCancelBtn"),
  adminModalSaveBtn: document.getElementById("adminModalSaveBtn"),
};

let currentProduct = null;

async function init() {
  attachEvents();
  await loadProducts();
  updateShippingOptions();
  renderCart();
}

function attachEvents() {
  dom.searchBtn.addEventListener("click", () => {
    state.searchTerm = dom.searchInput.value.trim();
    renderProducts();
  });

  dom.closeProductModal.addEventListener("click", hideProductModal);
  dom.modalAddBtn.addEventListener("click", () => {
    if (currentProduct) {
      addToCart(currentProduct.id);
    }
  });
  dom.checkoutBtn.addEventListener("click", openPaymentModal);
  dom.closePaymentModal.addEventListener("click", hidePaymentModal);
  dom.confirmPaymentBtn.addEventListener("click", confirmPayment);
  dom.discountCode.addEventListener("input", () => {
    state.discountCode = dom.discountCode.value.trim();
  });

  dom.openLoginBtn.addEventListener("click", showLoginModal);
  dom.closeLoginModal.addEventListener("click", hideLoginModal);
  dom.loginBtn.addEventListener("click", submitLogin);
  dom.logoutBtn.addEventListener("click", logout);
  dom.loginRole.addEventListener("change", () => {
    dom.loginUsername.value = dom.loginRole.value;
    dom.loginPassword.value = "";
    dom.loginModalMessage.textContent = "";
  });
  dom.manageProductsBtn.addEventListener("click", () => {
    dom.adminPanel.classList.toggle("hidden");
    if (!dom.adminPanel.classList.contains("hidden")) {
      renderAdminPanel();
    }
  });

  dom.adminTabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      dom.adminTabs.forEach((btn) => btn.classList.remove("active"));
      tab.classList.add("active");
      state.adminTab = tab.dataset.tab;
      renderAdminPanel();
    });
  });

  dom.closeAdminEditModal.addEventListener("click", closeAdminEditModal);
  dom.adminModalCancelBtn.addEventListener("click", closeAdminEditModal);
  dom.adminModalSaveBtn.addEventListener("click", saveAdminProduct);

  // 使用事件委托處理管理面板按鈕
  dom.adminContent.addEventListener("click", (e) => {
    if (e.target.id === "adminAddProductBtn") {
      console.log("點擊新增商品按鈕");
      openAdminEditModal(null);
    } else if (e.target.classList.contains("admin-edit-btn")) {
      openAdminEditModal(e.target.dataset.id);
    } else if (e.target.classList.contains("admin-delete-btn")) {
      deleteAdminProduct(e.target.dataset.id);
    }
  });
}

async function loadProducts() {
  try {
    const response = await fetch(`${API_BASE_URL}/products`);
    state.products = await response.json();
    renderCategories();
    renderProducts();
  } catch (error) {
    showMessage("無法載入商品資料，請稍後重試。", true);
  }
}

function showLoginModal() {
  dom.loginModal.classList.remove("hidden");
  dom.loginModalMessage.textContent = "";
  dom.loginUsername.focus();
}

function hideLoginModal() {
  dom.loginModal.classList.add("hidden");
  dom.loginModalMessage.textContent = "";
}

async function submitLogin() {
  const username = dom.loginUsername.value.trim();
  const password = dom.loginPassword.value;

  if (!username || !password) {
    dom.loginModalMessage.textContent = "請輸入帳號與密碼。";
    return;
  }

  try {
    const res = await fetch(`${API_BASE_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    const data = await res.json();
    if (!res.ok) {
      dom.loginModalMessage.textContent = data.message || "登入失敗，請檢查帳號密碼。";
      return;
    }
    state.user = {
      userId: data.userId || username,
      name: data.name || username,
      role: data.role || "客戶",
    };
    updateUserUI();
    hideLoginModal();
    showMessage(`歡迎，${state.user.name}！`);
  } catch (error) {
    dom.loginModalMessage.textContent = "無法連線至後端，請稍後重試。";
  }
}

function logout() {
  state.user = null;
  updateUserUI();
  showMessage("已登出。", false);
}

function updateUserUI() {
  const loggedIn = !!state.user;
  const isAdmin = loggedIn && state.user.role === "管理員";

  dom.openLoginBtn.classList.toggle("hidden", loggedIn);
  dom.currentUser.classList.toggle("hidden", !loggedIn);
  dom.manageProductsBtn.classList.toggle("hidden", !isAdmin);

  // 管理員不顯示結帳按鈕與商品詳情的加入購物車按鈕
  dom.checkoutBtn.classList.toggle("hidden", isAdmin);
  dom.modalAddBtn.classList.toggle("hidden", isAdmin);

  if (loggedIn) {
    dom.displayName.textContent = `${state.user.name} (${state.user.role})`;
  }
  if (!loggedIn) {
    dom.displayName.textContent = "";
    dom.adminPanel.classList.add("hidden");
  } else if (!dom.adminPanel.classList.contains("hidden")) {
    renderAdminPanel();
  }

  // 重新渲染商品卡片以更新「加入購物車」按鈕的顯示狀態
  renderProducts();
}

function updateCheckoutUserInfo() {
  if (state.user) {
    dom.checkoutUserInfo.innerHTML = `目前登入：<strong>${state.user.name}</strong> (${state.user.role})`;
    dom.checkoutName.value = state.user.name;
  } else {
    dom.checkoutUserInfo.innerHTML = `目前未登入，結帳為訪客模式。`;
  }
}

function renderAdminPanel() {
  if (!state.user || state.user.role !== "管理員") {
    dom.adminContent.innerHTML = `<p>需要管理員權限才能使用此頁面。</p>`;
    return;
  }
  if (state.adminTab === "products") {
    renderAdminProducts();
  } else if (state.adminTab === "customers") {
    dom.adminContent.innerHTML = `<p>目前尚未實作客戶管理，請使用商品管理。</p>`;
  } else if (state.adminTab === "discounts") {
    dom.adminContent.innerHTML = `<p>目前尚未實作折扣管理，請使用商品管理。</p>`;
  } else {
    dom.adminContent.innerHTML = `<p>未知管理頁籤。</p>`;
  }
}

function renderAdminProducts() {
  dom.adminContent.innerHTML = `
    <div class="admin-section">
      <div class="admin-toolbar" style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
        <h3>商品管理</h3>
        <button id="adminAddProductBtn" class="primary">新增商品</button>
      </div>
      <div class="admin-product-list">
        ${state.products
          .map(
            (product) => `
          <div class="admin-product-row" style="display:flex;justify-content:space-between;align-items:center;padding:12px;border:1px solid #ddd;border-radius:6px;margin-bottom:8px;">
            <div>
              <strong>${product.name}</strong> (${product.category || "一般"})
              <div>價格: $${Number(product.price).toFixed(0)} / 庫存: ${product.stock}</div>
            </div>
            <div style="display:flex;gap:8px;">
              <button class="secondary admin-edit-btn" data-id="${product.id}">編輯</button>
              <button class="secondary admin-delete-btn" data-id="${product.id}">刪除</button>
            </div>
          </div>
        `,
          )
          .join("")}
      </div>
    </div>
  `;

  const addBtn = document.getElementById("adminAddProductBtn");
  if (addBtn) {
    console.log("adminAddProductBtn 按鈕已找到");
  } else {
    console.warn("找不到 adminAddProductBtn 按鈕");
  }
}

function openAdminEditModal(productId) {
  if (!productId) {
    state.adminEditingProduct = {
      id: Date.now().toString(),
      name: "",
      category: "一般",
      price: 0,
      stock: 0,
      description: "",
      images: [],
    };
  } else {
    const existing = state.products.find((product) => String(product.id) === String(productId));
    state.adminEditingProduct = existing
      ? { ...existing, images: existing.images || [] }
      : null;
  }
  if (!state.adminEditingProduct) {
    showMessage("找不到要編輯的商品。", true);
    return;
  }
  dom.adminModalName.value = state.adminEditingProduct.name || "";
  dom.adminModalCategory.value = state.adminEditingProduct.category || "一般";
  dom.adminModalPrice.value = state.adminEditingProduct.price || 0;
  dom.adminModalStock.value = state.adminEditingProduct.stock || 0;
  dom.adminModalImages.value = (state.adminEditingProduct.images || []).join(", ");
  dom.adminEditModal.classList.remove("hidden");
}

function closeAdminEditModal() {
  state.adminEditingProduct = null;
  dom.adminEditModal.classList.add("hidden");
}

async function saveAdminProduct() {
  if (!state.adminEditingProduct) {
    return;
  }
  const name = dom.adminModalName.value.trim();
  const category = dom.adminModalCategory.value.trim() || "一般";
  const price = Number(dom.adminModalPrice.value) || 0;
  const stock = Number(dom.adminModalStock.value) || 0;
  const images = dom.adminModalImages.value
    .split(",")
    .map((url) => url.trim())
    .filter(Boolean);

  if (!name) {
    showMessage("商品名稱不可為空。", true);
    return;
  }

  const edited = {
    ...state.adminEditingProduct,
    name,
    category,
    price,
    stock,
    images,
  };

  const existingIndex = state.products.findIndex((product) => String(product.id) === String(edited.id));
  if (existingIndex >= 0) {
    state.products[existingIndex] = edited;
  } else {
    state.products.unshift(edited);
  }
  closeAdminEditModal();
  renderAdminPanel();
  renderProducts();
  await syncProductsToCsv();
  showMessage(`商品 ${name} 已儲存。`, false);
}

async function deleteAdminProduct(productId) {
  state.products = state.products.filter((product) => String(product.id) !== String(productId));
  renderAdminPanel();
  renderProducts();
  await syncProductsToCsv();
  showMessage("商品已刪除。", false);
}

async function renderCategories() {
  const categories = ["所有", ...new Set(state.products.map((product) => product.category || "一般"))];
  dom.categoryList.innerHTML = categories
    .map((category) => `
      <li><button class="category-button ${state.activeCategory === category ? "active" : ""}" data-category="${category}">${category}</button></li>
    `)
    .join("");

  dom.categoryList.querySelectorAll("button").forEach((button) => {
    button.addEventListener("click", () => {
      state.activeCategory = button.dataset.category;
      renderProducts();
    });
  });
}

function getFilteredProducts() {
  return state.products.filter((product) => {
    const matchesCategory = state.activeCategory === "所有" || product.category === state.activeCategory;
    const matchesSearch = state.searchTerm === "" || product.name.toLowerCase().includes(state.searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });
}

function renderProducts() {
  const list = getFilteredProducts();
  dom.products.innerHTML = list
    .map((product) => {
      return `
        <article class="card">
          <img src="${product.images[0] || "https://via.placeholder.com/300"}" alt="${product.name}">
          <div class="card-body">
            <h3>${product.name}</h3>
            <p>${product.description || ""}</p>
            <p>類別：${product.category || "一般"}</p>
            <p>價格：$${product.price.toFixed(0)}</p>
            <p>庫存：${product.stock}</p>
            ${state.user && state.user.role === "管理員" ? "" : `<button class="primary" data-id="${product.id}">加入購物車</button>`}
          </div>
        </article>
      `;
    })
    .join("");

  dom.products.querySelectorAll("button[data-id]").forEach((button) => {
    button.addEventListener("click", () => addToCart(button.dataset.id));
  });
}

function openProductModal(productId) {
  const product = state.products.find((item) => String(item.id) === String(productId));
  if (!product) {
    return;
  }
  currentProduct = product;
  dom.modalProductName.textContent = product.name;
  dom.modalProductDesc.textContent = product.description || "";
  dom.modalProductPrice.textContent = `$${product.price.toFixed(0)}`;
  dom.modalProductCategory.textContent = product.category || "一般";
  dom.modalProductStock.textContent = product.stock;
  dom.productModal.classList.remove("hidden");
}

function hideProductModal() {
  dom.productModal.classList.add("hidden");
}

function addToCart(productId) {
  // 未登入則要求登入顧客帳號
  if (!state.user || state.user.role !== "客戶") {
    showMessage("請先登入顧客帳號再進行購物。", true);
    showLoginModal();
    return;
  }

  const product = state.products.find((item) => String(item.id) === String(productId));
  if (!product) {
    showMessage("找不到指定商品。", true);
    return;
  }
  const existing = state.cart.find((item) => String(item.id) === String(productId));
  if (existing) {
    existing.qty += 1;
  } else {
    state.cart.push({ id: product.id, name: product.name, price: product.price, qty: 1 });
  }
  hideProductModal();
  renderCart();
  showMessage(`已加入 ${product.name}。`, false);
}

function removeFromCart(productId) {
  state.cart = state.cart.filter((item) => String(item.id) !== String(productId));
  renderCart();
}

function changeQty(productId, delta) {
  const item = state.cart.find((entry) => String(entry.id) === String(productId));
  if (!item) {
    return;
  }
  item.qty = Math.max(1, item.qty + delta);
  renderCart();
}

async function renderCart() {
  dom.cart.innerHTML = state.cart.length
    ? state.cart
        .map((item) => `
          <div class="cart-row">
            <div>
              <strong>${item.name}</strong>
              <div>$${item.price.toFixed(0)} x ${item.qty}</div>
            </div>
            <div class="cart-actions">
              <button class="secondary" data-action="decrease" data-id="${item.id}">-</button>
              <button class="secondary" data-action="increase" data-id="${item.id}">+</button>
              <button class="secondary" data-action="remove" data-id="${item.id}">移除</button>
            </div>
          </div>
        `)
        .join("")
    : `<p>購物車目前沒有商品。</p>`;

  dom.cart.querySelectorAll("button[data-action]").forEach((button) => {
    const action = button.dataset.action;
    const id = button.dataset.id;
    button.addEventListener("click", () => {
      if (action === "remove") {
        removeFromCart(id);
      } else if (action === "increase") {
        changeQty(id, 1);
      } else if (action === "decrease") {
        changeQty(id, -1);
      }
    });
  });

  await refreshCartSummary();
}

async function refreshCartSummary() {
  const payload = {
    cartItems: state.cart.map((item) => ({ id: item.id, qty: item.qty })),
    discountCode: state.discountCode,
    shippingMethod: state.shippingMethod,
  };
  try {
    const res = await fetch(`${API_BASE_URL}/cart/calculate`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = await res.json();
    state.totals = data;
    dom.subtotal.textContent = data.subtotal.toFixed(0);
    dom.discount.textContent = data.discount.toFixed(0);
    dom.shipping.textContent = data.shipping.toFixed(0);
    dom.total.textContent = data.total.toFixed(0);
  } catch (error) {
    showMessage("購物車計算失敗，請稍後重試。", true);
  }
}

function openPaymentModal() {
  if (!state.user || state.user.role !== "客戶") {
    showMessage("請先登入顧客帳號。", true);
    showLoginModal();
    return;
  }
  if (!state.cart.length) {
    showMessage("購物車沒有商品，無法結帳。", true);
    return;
  }
  updateCheckoutUserInfo();
  dom.paymentModal.classList.remove("hidden");
}

function hidePaymentModal() {
  dom.paymentModal.classList.add("hidden");
  dom.paymentMessage.textContent = "";
}

function updateShippingOptions() {
  dom.shippingSelect.innerHTML = `
    <option value="flat">固定運費 $60</option>
    <option value="freeOver">滿 3000 免運，否則 $80</option>
  `;
  dom.shippingSelect.addEventListener("change", () => {
    state.shippingMethod = dom.shippingSelect.value;
    renderCart();
  });
}

async function confirmPayment() {
  state.paymentMethod = dom.paymentMethod.value;
  state.paymentAccount = dom.checkoutCard.value.trim();
  state.discountCode = dom.discountCode.value.trim();

  const payload = {
    cartItems: state.cart.map((item) => ({ id: item.id, qty: item.qty })),
    discountCode: state.discountCode,
    shippingMethod: state.shippingMethod,
    paymentMethod: state.paymentMethod,
    paymentAccount: state.paymentAccount,
  };

  try {
    const res = await fetch(`${API_BASE_URL}/cart/checkout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = await res.json();
    if (!res.ok) {
      dom.paymentMessage.textContent = data.message || "付款失敗。";
      return;
    }

    // 更新前端庫存數並回寫 CSV
    state.cart.forEach((item) => {
      const product = state.products.find((product) => String(product.id) === String(item.id));
      if (product) {
        product.stock = Math.max(0, Number(product.stock) - Number(item.qty));
      }
    });
    await syncProductsToCsv();

    showMessage(`付款成功！訂單編號 ${data.orderId}`, false);
    state.cart = [];
    hidePaymentModal();
    renderCart();
    renderProducts();
  } catch (error) {
    dom.paymentMessage.textContent = "無法連線至後端。";
  }
}

async function syncProductsToCsv() {
  try {
    const response = await fetch(`${API_BASE_URL}/products/sync`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(
        state.products.map((product) => ({
          id: String(product.id),
          name: product.name,
          price: product.price,
          stock: product.stock,
        })),
      ),
    });
    const data = await response.json();
    if (!response.ok || !data.success) {
      showMessage(`同步 CSV 失敗：${data.message || "未知錯誤"}`, true);
      return false;
    }
    return true;
  } catch (error) {
    showMessage("無法連線至後端進行 CSV 同步。", true);
    return false;
  }
}

function showMessage(text, isError) {
  dom.message.textContent = text;
  dom.message.classList.toggle("error", !!isError);
  if (!text) {
    dom.message.classList.remove("error");
  }
}

init();
