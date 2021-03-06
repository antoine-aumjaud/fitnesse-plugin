/**
 * JS from FitNesse project (fitnesse.js)
 * Modification done: 
 * - keep only js on collapsible section
 * - change article by container id in selectors (#fitnesse_results)
 */

$(function(){

$(document)
	
	.on("touchstart click", "#fitnesse_results .collapsible > p.title", function () {
		$(this).parent().toggleClass('closed');
	})
	.on("click", "#fitnesse_results .collapsible > p.title a", function (event) {
		// Do not open section when clicking on a link in the title, just follow the link.
		event.stopPropagation();
		return true;
	})
	.on('click', '#fitnesse_results .collapsible .expandall', function () {
		var section = $(this).closest('.collapsible');
		section.find('.collapsible').andSelf().removeClass('closed');
		section.find('.scenario').removeClass('closed').next().show();
		return false;
	})
	.on('click', '#fitnesse_results .collapsible .collapseall', function () {
		var section = $(this).closest('.collapsible');
		section.find('.collapsible, .scenario').andSelf().addClass('closed');
		section.find('.scenario').addClass('closed').next().hide();
		return false;
	})
	
	;
});


