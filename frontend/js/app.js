const BASE_URL = "http://localhost:8081";

function login(){

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch(BASE_URL + "/auth/login", {
        method:"POST",
        headers:{
            "Content-Type":"application/json"
        },
        body: JSON.stringify({
            username,
            password
        })
    })
    .then(res => res.json())
    .then(data => {

        if(data.token){

            localStorage.setItem("token", data.token);

            window.location.href = "dashboard.html";

        }else{
            document.getElementById("message").innerText = "Invalid credentials";
        }
    })
    .catch(()=>{
        document.getElementById("message").innerText = "Server error";
    });
}

function parseJwt(token){
    return JSON.parse(atob(token.split('.')[1]));
}


function fetchBooks(){

    const token = localStorage.getItem("token");

    const payload = parseJwt(token);   // ⭐ decode role
    console.log("ROLE:", payload.role);

    fetch("http://localhost:8081/books", {

        headers:{
            "Authorization": "Bearer " + token
        }
    })
    .then(res => res.json())
    .then(data => {

        const container = document.getElementById("books-container");
        container.innerHTML = "";

        data.forEach(book => {

            // ⭐ create delete button ONLY for admin
            let deleteButton = "";

            if(payload.role === "ROLE_ADMIN"){
                deleteButton = `
                    <button 
                        onclick="deleteBook(${book.id})"
                        style="margin-top:10px;
                               background:red;
                               color:white;
                               border:none;
                               padding:8px;
                               border-radius:6px;
                               cursor:pointer;">
                        Delete
                    </button>
                `;
            }

            container.innerHTML += `
                <div class="book-card">
                    <h3>${book.title}</h3>
                    <p>Author: ${book.author}</p>
                    ${deleteButton}
                </div>
            `;
        });
    });
}


function logout(){

    localStorage.removeItem("token");

    window.location.href = "login.html";
}

function showAddBookForm(){
    document.getElementById("addBookForm").style.display = "block";
}

function addBook(){

    const token = localStorage.getItem("token");

    fetch("http://127.0.0.1:8081/books/add",{

        method:"POST",

        headers:{
            "Content-Type":"application/json",
            "Authorization":"Bearer " + token
        },

        body: JSON.stringify({
            title: document.getElementById("title").value,
            author: document.getElementById("author").value,
            availableCopies: document.getElementById("copies").value
        })
    })
    .then(res => {

        if(res.status === 403){
            alert("Only ADMIN can add books!");
            return;
        }

        return res.json();
    })
    .then(() => {

        fetchBooks();

        document.getElementById("addBookForm").style.display = "none";
    });
}

function deleteBook(id){

    const token = localStorage.getItem("token");

    fetch(`http://127.0.0.1:8081/books/delete/${id}`,{

        method:"DELETE",

        headers:{
            "Authorization":"Bearer " + token
        }

    })
    .then(res => {

        console.log("DELETE STATUS:", res.status);

        if(res.status === 403){
            alert("Only ADMIN can delete!");
            return;
        }

        fetchBooks(); // reload books
    });
}


function parseJwt(token){

    return JSON.parse(atob(token.split('.')[1]));
}

function register(){

    const username = document.getElementById("regUsername").value;
    const password = document.getElementById("regPassword").value;

    fetch("http://127.0.0.1:8081/auth/register", {

        method:"POST",
        headers:{
            "Content-Type":"application/json"
        },
        body: JSON.stringify({ username, password })

    })
    .then(async res => {

        const message = await res.text();

        if(res.status === 200){

            document.getElementById("regMessage").style.color = "green";
            document.getElementById("regMessage").innerText = message;

            setTimeout(() => {
                window.location.href = "login.html";
            }, 1500);

        } 
        else if(res.status === 409){

            document.getElementById("regMessage").style.color = "red";
            document.getElementById("regMessage").innerText = message;
        }
        else{

            document.getElementById("regMessage").innerText = "Something went wrong.";
        }
    });
}

