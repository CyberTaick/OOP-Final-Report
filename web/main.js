// 簡單前端邏輯：商品、購物車、登入、商品詳情、付款模態
(function(){
  const products = [
    {id:1,name:'無線滑鼠',price:399,category:'周邊',stock:12,description:'高精度無線滑鼠，具備可自訂按鍵與人體工學設計。',images:['https://via.placeholder.com/280x160?text=Mouse+1','https://via.placeholder.com/280x160?text=Mouse+2','https://via.placeholder.com/280x160?text=Mouse+3']},
    {id:2,name:'機械鍵盤',price:1990,category:'鍵盤',stock:5,description:'青軸機械鍵盤，耐用按鍵與 RGB 燈效，適合長時間打字與遊戲。',images:['https://via.placeholder.com/280x160?text=Keyboard+1','https://via.placeholder.com/280x160?text=Keyboard+2','https://via.placeholder.com/280x160?text=Keyboard+3']},
    {id:3,name:'USB-C 充電器',price:350,category:'充電',stock:20,description:'支援 65W 快充，體積小巧，適用筆電與手機快速充電。',images:['https://via.placeholder.com/280x160?text=Charger+1','https://via.placeholder.com/280x160?text=Charger+2','https://via.placeholder.com/280x160?text=Charger+3']}
  ];

  const categories = ['所有','周邊','鍵盤','充電'];

  const shippingOptions = [
    {id:'flat',name:'固定運費 ($60)',calc:(subtotal)=>60},
    {id:'freeOver',name:'滿額免運 ($1000)',calc:(subtotal)=> subtotal>=1000?0:80}
  ];

  const discounts = {
    'SAVE10': {type:'percent',value:10},
    'FLAT50': {type:'flat',value:50}
  };

  const accounts = {
    admin: {username:'admin',password:'admin123',role:'管理員'},
    customer: {username:'customer',password:'cust123',role:'客戶'}
  };

  const state = {
    cart:[],
    shipping:'flat',
    discountCode:null,
    category:'所有',
    search:'',
    user:null,
    currentProductId:null,
    adminEditingId:null
  };

  function renderProducts(){
    const el = document.getElementById('products');
    el.innerHTML = '';
    const filtered = products.filter(p=>{
      if(state.category && state.category!=='所有' && p.category!==state.category) return false;
      if(state.search && !p.name.toLowerCase().includes(state.search.toLowerCase())) return false;
      return true;
    });

    filtered.forEach(p=>{
      const card = document.createElement('div');
      card.className = 'card';
      const imageCount = p.images && p.images.length ? Math.min(p.images.length, 2) : 1;
      const imageSources = p.images && p.images.length ? p.images.slice(0, imageCount) : ['https://via.placeholder.com/280x160?text=No+Image'];
      const imagesHtml = imageSources.map((src,index)=>`<img src="${src}" class="product-image" alt="${p.name} 圖片 ${index+1}">`).join('');
      card.innerHTML = `
        ${imagesHtml}
        <h3>${p.name}</h3>
        <p>價格: $${p.price}</p>
        <p class="cat">類別: ${p.category}</p>
        <p class="stock">庫存: ${p.stock}</p>
        <button data-id="${p.id}" class="addBtn primary">加入購物車</button>
      `;
      card.addEventListener('click', e => {
        if(e.target.classList.contains('addBtn')) return;
        openProductModal(p.id);
      });
      card.querySelector('.addBtn').addEventListener('click', e => {
        e.stopPropagation();
        addToCart(p.id);
      });
      el.appendChild(card);
    });
  }

  function renderCategories(){
    const ul = document.getElementById('categoryList');
    ul.innerHTML = '';
    categories.forEach(c=>{
      const li = document.createElement('li');
      li.textContent = c;
      if(c===state.category) li.classList.add('active');
      li.addEventListener('click', ()=>{
        state.category = c;
        renderCategories();
        renderProducts();
      });
      ul.appendChild(li);
    });
  }

  function addToCart(productId){
    const p = products.find(x=>x.id===productId);
    if(!p) return;
    const item = state.cart.find(i=>i.id===p.id);
    if(item){
      if(item.qty < p.stock) item.qty++;
    } else {
      state.cart.push({id:p.id,name:p.name,price:p.price,qty:1});
    }
    renderCart();
    showSystemMessage(`${p.name} 已加入購物車`);
  }

  function removeFromCart(productId){
    state.cart = state.cart.filter(i=>i.id!==productId);
    renderCart();
  }

  function calcSubtotal(){
    return state.cart.reduce((sum,item)=>sum + item.price * item.qty, 0);
  }

  function calcDiscount(subtotal){
    if(!state.discountCode) return 0;
    const d = discounts[state.discountCode.toUpperCase()];
    if(!d) return 0;
    return d.type==='percent' ? Math.round(subtotal * (d.value/100)) : d.value;
  }

  function calcShipping(subtotal){
    const option = shippingOptions.find(o=>o.id===state.shipping);
    return option ? option.calc(subtotal) : 0;
  }

  function renderCart(){
    const el = document.getElementById('cart');
    el.innerHTML = '';
    if(state.cart.length===0){
      el.textContent = '購物車目前沒有商品。';
    } else {
      state.cart.forEach(item => {
        const div = document.createElement('div');
        div.className = 'cart-item';
        div.innerHTML = `
          <div>${item.name} x ${item.qty}</div>
          <div>$${item.price*item.qty} <button data-id="${item.id}" class="remove">移除</button></div>
        `;
        el.appendChild(div);
      });
      el.querySelectorAll('.remove').forEach(btn => {
        btn.addEventListener('click', e => removeFromCart(Number(e.currentTarget.dataset.id)));
      });
    }

    const subtotal = calcSubtotal();
    const discount = calcDiscount(subtotal);
    const shipping = calcShipping(subtotal);
    const total = Math.max(0, subtotal - discount + shipping);

    document.getElementById('subtotal').textContent = subtotal;
    document.getElementById('discount').textContent = discount;
    document.getElementById('shipping').textContent = shipping;
    document.getElementById('total').textContent = total;
  }

  function openProductModal(productId){
    const product = products.find(p=>p.id===productId);
    if(!product) return;
    state.currentProductId = productId;
    document.getElementById('modalProductName').textContent = product.name;
    document.getElementById('modalProductDesc').textContent = product.description;
    document.getElementById('modalProductPrice').textContent = `$${product.price}`;
    document.getElementById('modalProductCategory').textContent = product.category;
    document.getElementById('modalProductStock').textContent = product.stock;
    const gallery = document.getElementById('productGallery');
    const images = product.images && product.images.length ? product.images : ['https://via.placeholder.com/280x160?text=No+Image'];
    gallery.innerHTML = images.map(src => `<img src="${src}" alt="${product.name} 圖片">`).join('');
    document.getElementById('productModal').classList.remove('hidden');
  }

  function closeProductModal(){
    document.getElementById('productModal').classList.add('hidden');
    state.currentProductId = null;
  }

  function renderAdminPanelTab(tabName){
    const content = document.getElementById('adminContent');
    const buttons = document.querySelectorAll('#adminPanel .admin-tabs button');
    buttons.forEach(btn => btn.classList.toggle('active', btn.dataset.tab === tabName));

    if(tabName === 'products'){
      const rows = products.map(p => `
        <tr>
          <td>${p.id}</td>
          <td>${p.name}</td>
          <td>${p.category}</td>
          <td>$${p.price}</td>
          <td>${p.stock}</td>
          <td>
            <button class="secondary admin-edit" data-id="${p.id}">編輯</button>
            <button class="secondary admin-delete" data-id="${p.id}">刪除</button>
          </td>
        </tr>
      `).join('');
      content.innerHTML = `
        <div class="admin-actions">
          <button id="adminAddProductBtn" class="primary">新增商品</button>
        </div>
        <div class="admin-table"><table><thead><tr><th>ID</th><th>商品</th><th>類別</th><th>價格</th><th>庫存</th><th>操作</th></tr></thead><tbody>${rows}</tbody></table></div>
        <div id="adminProductForm" class="admin-form hidden">
          <label>商品名稱<input id="adminProductName" type="text"></label>
          <label>類別<input id="adminProductCategory" type="text"></label>
          <label>價格<input id="adminProductPrice" type="number" min="0"></label>
          <label>庫存<input id="adminProductStock" type="number" min="0"></label>
          <label class="admin-form-full">描述<textarea id="adminProductDescription"></textarea></label>
          <label class="admin-form-full">圖片網址（逗號分隔）<input id="adminProductImages" type="text" placeholder="https://... , https://..."></label>
          <div class="admin-form-full" style="display:flex;justify-content:flex-end;gap:12px">
            <button id="adminCancelBtn" class="secondary">取消</button>
            <button id="adminSaveBtn" class="primary">儲存商品</button>
          </div>
        </div>
      `;
    } else if(tabName === 'customers'){
      const customers = Object.values(accounts).filter(a => a.role === '客戶');
      const rows = customers.map(c => `
        <tr>
          <td>${c.username}</td>
          <td>${c.role}</td>
          <td>${c.name}</td>
        </tr>
      `).join('');
      content.innerHTML = `<div class="admin-table"><table><thead><tr><th>帳號</th><th>身分</th><th>名稱</th></tr></thead><tbody>${rows}</tbody></table></div>`;
    } else {
      const rows = Object.entries(discounts).map(([code, data]) => `
        <tr>
          <td>${code}</td>
          <td>${data.type === 'percent' ? `${data.value}%` : `$${data.value}`}</td>
          <td>${data.type === 'percent' ? '百分比折扣' : '固定金額折扣'}</td>
        </tr>
      `).join('');
      content.innerHTML = `<div class="admin-table"><table><thead><tr><th>折扣碼</th><th>折扣</th><th>說明</th></tr></thead><tbody>${rows}</tbody></table></div>`;
    }

    attachAdminEventHandlers();
  }

  function attachAdminEventHandlers(){
    const addBtn = document.getElementById('adminAddProductBtn');
    const form = document.getElementById('adminProductForm');
    const saveBtn = document.getElementById('adminSaveBtn');
    const cancelBtn = document.getElementById('adminCancelBtn');

    if(addBtn){
      addBtn.addEventListener('click', ()=> openAdminProductForm());
    }
    if(saveBtn){
      saveBtn.addEventListener('click', saveAdminProduct);
    }
    if(cancelBtn){
      cancelBtn.addEventListener('click', (e)=>{ e.preventDefault(); closeAdminProductForm(); });
    }
    document.querySelectorAll('.admin-edit').forEach(btn => {
      btn.addEventListener('click', ()=> openAdminProductForm(Number(btn.dataset.id)));
    });
    document.querySelectorAll('.admin-delete').forEach(btn => {
      btn.addEventListener('click', ()=> deleteAdminProduct(Number(btn.dataset.id)));
    });
  }

  function openAdminProductForm(productId){
    const form = document.getElementById('adminProductForm');
    const isEdit = typeof productId === 'number';
    state.adminEditingId = isEdit ? productId : null;
    const product = products.find(p=>p.id===productId) || {name:'',category:'',price:'',stock:'',description:'',images:[]};
    document.getElementById('adminProductName').value = product.name;
    document.getElementById('adminProductCategory').value = product.category;
    document.getElementById('adminProductPrice').value = product.price;
    document.getElementById('adminProductStock').value = product.stock;
    document.getElementById('adminProductDescription').value = product.description;
    document.getElementById('adminProductImages').value = product.images ? product.images.join(', ') : '';
    form.classList.remove('hidden');
  }

  function closeAdminProductForm(){
    const form = document.getElementById('adminProductForm');
    if(form) form.classList.add('hidden');
    state.adminEditingId = null;
  }

  function saveAdminProduct(){
    const name = document.getElementById('adminProductName').value.trim();
    const category = document.getElementById('adminProductCategory').value.trim();
    const price = Number(document.getElementById('adminProductPrice').value);
    const stock = Number(document.getElementById('adminProductStock').value);
    const description = document.getElementById('adminProductDescription').value.trim();
    const images = document.getElementById('adminProductImages').value.split(',').map(s=>s.trim()).filter(Boolean);

    if(!name || !category || isNaN(price) || isNaN(stock)){
      alert('請填寫完整商品名稱、類別、價格與庫存。');
      return;
    }

    if(state.adminEditingId){
      const product = products.find(p=>p.id===state.adminEditingId);
      if(product){
        product.name = name;
        product.category = category;
        product.price = price;
        product.stock = stock;
        product.description = description;
        product.images = images;
      }
    } else {
      const nextId = products.reduce((max,p)=>Math.max(max,p.id),0)+1;
      products.push({id:nextId, name, category, price, stock, description, images});
      if(!categories.includes(category)) categories.push(category);
    }

    closeAdminProductForm();
    renderCategories();
    renderProducts();
    renderAdminPanelTab('products');
  }

  function deleteAdminProduct(productId){
    if(!confirm('確定要刪除這項商品嗎？')) return;
    const index = products.findIndex(p=>p.id===productId);
    if(index >= 0){
      products.splice(index,1);
      renderCategories();
      renderProducts();
      renderAdminPanelTab('products');
    }
  }

  function displayUserInfo(){
    const badge = document.getElementById('currentUser');
    const nameEl = document.getElementById('displayName');
    const loginBtn = document.getElementById('openLoginBtn');
    const adminPanel = document.getElementById('adminPanel');

    if(state.user){
      nameEl.textContent = `${state.user.name} (${state.user.role})`;
      badge.classList.remove('hidden');
      loginBtn.classList.add('hidden');
    } else {
      badge.classList.add('hidden');
      loginBtn.classList.remove('hidden');
    }

    if(state.user && state.user.role === '管理員'){
      adminPanel.classList.remove('hidden');
      renderAdminPanelTab('products');
    } else {
      adminPanel.classList.add('hidden');
    }
  }

  function openLoginModal(){
    document.getElementById('loginModal').classList.remove('hidden');
    document.getElementById('loginModalMessage').textContent = '';
  }

  function closeLoginModal(){
    document.getElementById('loginModal').classList.add('hidden');
  }

  function openPaymentModal(){
    if(!state.user) return showSystemMessage('請先登入後再結帳', true);
    if(state.cart.length===0) return showSystemMessage('購物車為空', true);
    document.getElementById('checkoutUserInfo').textContent = `登入身份：${state.user.name} (${state.user.role})`;
    document.getElementById('checkoutName').value = state.user.name;
    document.getElementById('checkoutCard').value = '';
    document.getElementById('discountCode').value = state.discountCode || '';
    document.getElementById('shippingSelect').value = state.shipping;
    document.getElementById('paymentModal').classList.remove('hidden');
  }

  function closePaymentModal(){
    document.getElementById('paymentModal').classList.add('hidden');
    document.getElementById('paymentMessage').textContent = '';
  }

  function applyDiscountInModal(code){
    if(!code) return showPaymentMessage('請輸入折扣碼', true);
    const valid = discounts[code.toUpperCase()];
    if(!valid) return showPaymentMessage('折扣碼無效', true);
    state.discountCode = code.toUpperCase();
    renderCart();
    showPaymentMessage('折扣已套用');
  }

  function confirmPayment(){
    const method = document.getElementById('paymentMethod').value;
    const name = document.getElementById('checkoutName').value.trim();
    const cardOrAccount = document.getElementById('checkoutCard').value.trim();
    const shipping = document.getElementById('shippingSelect').value;

    if(!name) return showPaymentMessage('請輸入收件姓名', true);
    if(!cardOrAccount) return showPaymentMessage('請輸入付款資訊', true);

    state.shipping = shipping;
    renderCart();
    showPaymentMessage('付款中，請稍候...');

    const details = {
      user: state.user,
      paymentMethod: method,
      name,
      account: cardOrAccount,
      cart: state.cart.slice(),
      total: document.getElementById('total').textContent,
      shipping: state.shipping,
      discountCode: state.discountCode
    };

    mockProcessPayment(details).then(result => {
      if(result.success){
        closePaymentModal();
        state.cart = [];
        state.discountCode = null;
        renderCart();
        showSystemMessage(`付款成功，訂單編號: ${result.orderId}`);
      } else {
        showPaymentMessage('付款失敗，請稍後再試', true);
      }
    });
  }

  function login(){
    const role = document.getElementById('loginRole').value;
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;
    const account = accounts[role];

    if(!username || !password) return showLoginMessage('請輸入帳號與密碼', true);
    if(username !== account.username || password !== account.password) return showLoginMessage('帳號或密碼錯誤', true);

    state.user = {name: account.username, role: account.role};
    displayUserInfo();
    closeLoginModal();
    showSystemMessage(`已以 ${state.user.role} 身份登入`);
  }

  function logout(){
    state.user = null;
    displayUserInfo();
    showSystemMessage('已登出');
  }

  function initShippingOptions(){
    const sel = document.getElementById('shippingSelect');
    sel.innerHTML = '';
    shippingOptions.forEach(option => {
      const el = document.createElement('option');
      el.value = option.id;
      el.textContent = option.name;
      sel.appendChild(el);
    });
  }

  function showSystemMessage(msg, isError){
    const m = document.getElementById('message');
    m.textContent = msg;
    m.style.color = isError ? 'red' : 'green';
  }

  function showLoginMessage(msg, isError){
    const m = document.getElementById('loginModalMessage');
    m.textContent = msg;
    m.style.color = isError ? 'red' : 'green';
  }

  function showPaymentMessage(msg, isError){
    const m = document.getElementById('paymentMessage');
    m.textContent = msg;
    m.style.color = isError ? 'red' : 'green';
  }

  document.addEventListener('DOMContentLoaded', ()=>{
    renderCategories();
    renderProducts();
    renderCart();
    initShippingOptions();
    displayUserInfo();

    document.getElementById('openLoginBtn').addEventListener('click', openLoginModal);
    document.getElementById('loginBtn').addEventListener('click', login);
    document.getElementById('logoutBtn').addEventListener('click', logout);
    document.getElementById('closeLoginModal').addEventListener('click', closeLoginModal);
    document.getElementById('checkoutBtn').addEventListener('click', openPaymentModal);
    document.getElementById('closeProductModal').addEventListener('click', closeProductModal);
    document.getElementById('modalAddBtn').addEventListener('click', ()=>{
      if(state.currentProductId !== null){
        addToCart(state.currentProductId);
        closeProductModal();
      }
    });
    document.getElementById('closePaymentModal').addEventListener('click', closePaymentModal);
    document.getElementById('applyDiscountBtn').addEventListener('click', ()=>applyDiscountInModal(document.getElementById('discountCode').value));
    document.getElementById('confirmPaymentBtn').addEventListener('click', confirmPayment);
    document.querySelectorAll('#adminPanel .admin-tabs button').forEach(btn => {
      btn.addEventListener('click', () => renderAdminPanelTab(btn.dataset.tab));
    });

    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    searchInput.addEventListener('input', e => { state.search = e.target.value; renderProducts(); });
    searchBtn.addEventListener('click', ()=>{ state.search = searchInput.value; renderProducts(); });
  });
})();
