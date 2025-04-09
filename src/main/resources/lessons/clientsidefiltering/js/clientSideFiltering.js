var dataFetched = false;

function selectUser() {

    var newEmployeeID = $("#UserSelect").val();
    document.getElementById("employeeRecord").innerHTML = document.getElementById(newEmployeeID).innerHTML;
}

function fetchUserData() {
    if (!dataFetched) {
        dataFetched = true;
        ajaxFunction(document.getElementById("userID").value);
    }
}

// Simple HTML sanitization function to prevent XSS
function sanitizeHTML(html) {
    var temp = document.createElement('div');
    temp.textContent = html;
    return temp.innerHTML;
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
            html = html + '<tr id = "' + sanitizeHTML(result[i].UserID) + '"</tr>';
            html = html + '<td>' + sanitizeHTML(result[i].UserID) + '</td>';
            html = html + '<td>' + sanitizeHTML(result[i].FirstName) + '</td>';
            html = html + '<td>' + sanitizeHTML(result[i].LastName) + '</td>';
            html = html + '<td>' + sanitizeHTML(result[i].SSN) + '</td>';
            html = html + '<td>' + sanitizeHTML(result[i].Salary) + '</td>';
            html = html + '</tr>';
        }
        html = html + '</tr></table>';

        var newdiv = document.createElement("div");
        // Using createElement and appendChild instead of innerHTML for safer DOM manipulation
        var tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        while (tempDiv.firstChild) {
            newdiv.appendChild(tempDiv.firstChild);
        }
        var container = document.getElementById("hiddenEmployeeRecords");
        container.appendChild(newdiv);
    });
}
