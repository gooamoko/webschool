/**
 * Функция для выполнения AJAX-запроса с подменой содержимого компонента
 */
function fetchBlock(id, requestUrl) {
	$.ajax({
		url : requestUrl,
		type : 'GET',
		success : function(res) {
			$(id).fadeOut(function() {
				$(this).html(res).fadeIn();
			});
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log(jqXHR.statusText + "(" + jqXHR.status + ")" + textStatus + ": " + errorThrown);
			alert("Ошибка загрузки!\nОбратитесь к разработчику.");
		}
	});
}

function fetchAll(requestUrl) {
	$.ajax({
		url : requestUrl,
		type : 'GET',
		success : function(res) {
			$(html).fadeOut(function() {
				$(this).html(res).fadeIn();
			});
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log(jqXHR.statusText + " (" + jqXHR.status + ") " + textStatus + ": " + errorThrown);
			alert("Ошибка загрузки!\nОбратитесь к разработчику.");
		}
	});
}
