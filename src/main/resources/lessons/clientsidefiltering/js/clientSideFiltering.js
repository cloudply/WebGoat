var dataFetched = false;

function selectUser() {

    var newEmployeeID = $("#UserSelect").val();
    document.getElementById("employeeRecord").textContent = document.getElementById(newEmployeeID).textContent;
}

function fetchUserData() {
    if (!dataFetched) {
        dataFetched = true;
        ajaxFunction(document.getElementById("userID").value);
    }
}

function ajaxFunction(userId) {
    $.get("clientSideFiltering/salaries?userId=" + userId, function (result, status) {
        var html = "<table border = '1' width = '90%' align = 'center'";
        html = html + '<tr>';
        html = html + '<td>UserID</td>';
        html = html + '<td>First Name</td>';
        html = html + '<td>Last Name</td>';
        html = html + '<td>SSN</td>';
        html = html + '<td>Salary</td>';

        for (var i = 0; i < result.length; i++) {
            html = html + '<tr id = "' + encodeURIComponent(result[i].UserID) + '"</tr>';
            html = html + '<td>' + escapeHtml(result[i].UserID) + '</td>';
            html = html + '<td>' + escapeHtml(result[i].FirstName) + '</td>';
            html = html + '<td>' + escapeHtml(result[i].LastName) + '</td>';
            html = html + '<td>' + escapeHtml(result[i].SSN) + '</td>';
            html = html + '<td>' + escapeHtml(result[i].Salary) + '</td>';
            html = html + '</tr>';
        }
        html = html + '</tr></table>';

        var newdiv = document.createElement("div");
        // Using textContent and then setting innerHTML to avoid XSS
        var container = document.getElementById("hiddenEmployeeRecords");
        newdiv.textContent = ''; // Clear any content
        newdiv.innerHTML = html; // Set sanitized HTML
        container.appendChild(newdiv);
    });
}

// Helper function to escape HTML special characters
function escapeHtml(unsafe) {
    return unsafe
        .toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
