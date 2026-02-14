const API = "http://localhost:8081";

/* ================= PASSWORD TOGGLE ================= */
function togglePassword(){
    const input = document.getElementById("password");
    input.type = input.type === "password" ? "text" : "password";
}

/* ================= JWT PARSER ================= */
function parseJwt(token){
    return JSON.parse(atob(token.split('.')[1]));
}

/* ================= LOGIN ================= */
async function login(){

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const remember = document.getElementById("rememberMe").checked;
    const message = document.getElementById("message");

    if(!username || !password){
        message.innerText = "Enter credentials";
        return;
    }

    try{

        const res = await fetch(API + "/auth/login",{
            method:"POST",
            headers:{"Content-Type":"application/json"},
            body:JSON.stringify({username,password})
        });

        if(!res.ok) throw new Error();

        const data = await res.json();

        localStorage.setItem("token",data.token);

        const payload = parseJwt(data.token);
        localStorage.setItem("role",payload.role);

        if(remember){
            localStorage.setItem("savedUsername",username);
            localStorage.setItem("savedPassword",password);
        }

        window.location.href="dashboard.html";

    }catch{
        message.innerText="Invalid login";
    }
}

/* ================= AUTO FILL ================= */
document.addEventListener("DOMContentLoaded",()=>{

    // ---------- Autofill ----------
    const u = localStorage.getItem("savedUsername");
    const p = localStorage.getItem("savedPassword");

    if(u){
        document.getElementById("username").value = u;
        document.getElementById("password").value = p;
        document.getElementById("rememberMe").checked = true;
    }

    // ---------- ENTER KEY LOGIN ----------
    document.addEventListener("keydown", function(event){
        if(event.key === "Enter"){
            login();
        }
    });

});

function initDashboard(){

    const token = localStorage.getItem("token");

    if(!token){
        window.location.href="login.html";
        return;
    }

    showRole();
    loadBooks();
}

function getRole(){
    return localStorage.getItem("role") || "";
}

function isAdmin(){
    return getRole().includes("ADMIN");
}

function showRole(){
    const adminLink = document.getElementById("adminLink");
if(adminLink && !isAdmin()){
    adminLink.style.display="none";
}

    const label = document.getElementById("roleLabel");

    if(label){
        label.innerText = isAdmin()
            ? "Admin"
            : "User";
    }
}

function loadBooks(){

    fetch(API + "/books",{
        headers:{
            "Authorization":"Bearer " + localStorage.getItem("token")
        }
    })
    .then(res=>{
        if(res.status===401) logout();
        return res.json();
    })
    .then(books => renderBooks(books));
}

function renderBooks(data){

    const container =
        document.getElementById("books-container");

    container.innerHTML = "";

    data.forEach(book => {

        let buttons = "";

        if(!isAdmin()){
            buttons += `
                <button class="action-btn"
                onclick="issueBook(${book.id})">
                Issue
                </button>`;
        }

        if(isAdmin()){
            buttons += `
                <button class="delete-btn"
                onclick="deleteBook(${book.id})">
                Delete
                </button>`;
        }

        container.innerHTML += `
            <div class="book-card">
                <h3>${book.title}</h3>
                <p>${book.author}</p>
                ${buttons}
            </div>
        `;
    });
}
function searchBooks(){

    const token = localStorage.getItem("token");

    const title =
        document.getElementById("titleFilter").value;

    const author =
        document.getElementById("authorFilter").value;

    const category =
        document.getElementById("categoryFilter").value;

    const isbn =
        document.getElementById("isbnFilter").value;

    const params = new URLSearchParams();

    if(title) params.append("title", title);
    if(author) params.append("author", author);
    if(category) params.append("category", category);
    if(isbn) params.append("isbn", isbn);

    fetch(API + "/books/search?" + params.toString(),{
        headers:{
            "Authorization":"Bearer " + token
        }
    })
    .then(res => res.json())
    .then(data => renderBooks(data))
    .catch(err => console.error(err));
}


function issueBook(id){

    fetch(API + "/transactions/issue/" + id,{
        method:"POST",
        headers:{
            "Authorization":
            "Bearer " + localStorage.getItem("token")
        }
    })
    .then(()=>{
        alert("Book Issued Successfully");

        loadBooks();   // refresh books
        loadStats();   // ⭐ refresh admin stats
    });
}


function deleteBook(id){

    fetch(API + "/books/delete/" + id,{
        method:"DELETE",
        headers:{
            "Authorization":
            "Bearer " + localStorage.getItem("token")
        }
    })
    .then(()=>{
        alert("Book Deleted");

        loadBooks();
        loadStats();   // ⭐ refresh stats
    });
}




function logout(){
    localStorage.clear();
    window.location.href="login.html";
}

function initMyBooks(){

    const token = localStorage.getItem("token");

    if(!token){
        window.location.href="login.html";
        return;
    }

    loadMyBooks();
}

function loadMyBooks(){

    fetch(API + "/transactions/my-books",{
        headers:{
            "Authorization":"Bearer " + localStorage.getItem("token")
        }
    })
    .then(res=>{
        if(res.status===401) logout();
        return res.json();
    })
    .then(transactions=>{

        const container =
            document.getElementById("my-books-container");

        container.innerHTML="";

        if(transactions.length===0){
            container.innerHTML="<p>No books issued.</p>";
            return;
        }

        transactions.forEach(t=>{

            container.innerHTML += `
                <div class="book-card">
                    <h3>${t.book.title}</h3>
                    <p>Author: ${t.book.author}</p>

                    <p>Issued: ${t.issueDate}</p>
                    <p>Due: ${t.dueDate}</p>

                    <p>Fine: ₹${t.fine}</p>

                    ${
                        t.status === "ISSUED"
                        ? `<button class="return-btn"
                             onclick="returnBook(${t.id})">
                             Return Book
                           </button>`
                        : `<p>Returned ✅</p>`
                    }
                </div>
            `;
        });
    });
}

function returnBook(transactionId){

    fetch(API + "/transactions/return/" + transactionId,{
        method:"POST",
        headers:{
            "Authorization":
            "Bearer " + localStorage.getItem("token")
        }
    })
    .then(()=>{
        alert("Book Returned");

        loadMyBooks();
        loadHistory();
        loadStats();   // ⭐ update stats
    });
}


function initAdmin(){

    if(!isAdmin()){
        alert("Access denied");
        window.location.href="dashboard.html";
        return;
    }

    loadStats();
    loadAllTransactions();
}

function loadStats(){

    fetch(API + "/transactions/stats",{
        headers:{
            "Authorization":"Bearer " +
            localStorage.getItem("token")
        }
    })
    .then(res => {
        if(!res.ok){
            throw new Error("Stats request failed");
        }
        return res.json();
    })
    .then(stats => {

        console.log("Stats received:", stats);

        // ✅ Map backend fields correctly
        const totalBooks = stats.totalBooks ?? 0;
        const issuedBooks = stats.activeLoans ?? 0;
        const returnedBooks = totalBooks - issuedBooks;
        const totalFine = stats.totalFine ?? 0;

        document.getElementById("stats-container").innerHTML = `
            <div class="stat-card">
                <h3>Total Books</h3>
                <p>${totalBooks}</p>
            </div>

            <div class="stat-card">
                <h3>Issued Books</h3>
                <p>${issuedBooks}</p>
            </div>

            <div class="stat-card">
                <h3>Returned Books</h3>
                <p>${returnedBooks}</p>
            </div>

            <div class="stat-card">
                <h3>Total Fine</h3>
                <p>₹${totalFine}</p>
            </div>
        `;
    })
    .catch(err => console.error("Stats error:", err));
}

function loadAllTransactions(){

    fetch(API + "/transactions/all",{
        headers:{
            "Authorization":"Bearer " +
            localStorage.getItem("token")
        }
    })
    .then(res=>res.json())
    .then(data=>{

        const container =
            document.getElementById("admin-transactions");

        container.innerHTML="";

        data.forEach(t=>{

            container.innerHTML += `
                <div class="book-card">
                    <h3>${t.book.title}</h3>

                    <p>User: ${t.user.username}</p>
                    <p>Issued: ${t.issueDate}</p>
                    <p>Due: ${t.dueDate}</p>
                    <p>Status: ${t.status}</p>
                    <p>Fine: ₹${t.fine}</p>
                </div>
            `;
        });
    });
}

function addBook(){

    const title =
        document.getElementById("bookTitle").value;

    const author =
        document.getElementById("bookAuthor").value;

    const copies =
        document.getElementById("bookCopies").value;

    if(!title || !author || !copies){
        alert("Fill all fields");
        return;
    }

    fetch(API + "/books/add",{
        method:"POST",
        headers:{
            "Content-Type":"application/json",
            "Authorization":"Bearer " +
            localStorage.getItem("token")
        },
        body:JSON.stringify({
            title:title,
            author:author,
            availableCopies:copies
        })
    })
    .then(res=>{
        if(!res.ok) throw new Error();
        return res.json();
    })
    .then(()=>{
        alert("Book added successfully!");

        document.getElementById("bookTitle").value="";
        document.getElementById("bookAuthor").value="";
        document.getElementById("bookCopies").value="";

        loadBooks();      // refresh dashboard books
        loadStats();      // refresh stats
    })
    .catch(()=>alert("Failed to add book"));
}

function loadHistory(){

    fetch(API + "/transactions/my-books",{
        headers:{
            "Authorization":
            "Bearer " + localStorage.getItem("token")
        }
    })
    .then(res=>{
        if(res.status===401) logout();
        return res.json();
    })
    .then(transactions=>{

        const container =
            document.getElementById("history-container");

        container.innerHTML="";

        transactions.forEach(tx=>{

            const statusClass =
                tx.status === "RETURNED"
                ? "status-returned"
                : "status-issued";

            container.innerHTML += `
                <div class="history-card">

                    <h3>${tx.book.title}</h3>

                    <p><b>Issued:</b> ${tx.issueDate}</p>
                    <p><b>Due:</b> ${tx.dueDate}</p>
                    <p><b>Returned:</b>
                        ${tx.returnDate ? tx.returnDate : "Not returned"}
                    </p>

                    <p class="${statusClass}">
                        ${tx.status}
                    </p>

                    <p><b>Fine:</b> ₹${tx.fine}</p>

                </div>
            `;
        });
    })
    .catch(err=>console.error(err));
}
