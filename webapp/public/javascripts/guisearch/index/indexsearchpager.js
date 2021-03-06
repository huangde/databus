(function ($) {
	AjaxSolr.CurrentPagerWidget = AjaxSolr.PagerWidget.extend({
		init: function(){
			this._super( false );
		},
		afterRequest: function () {
			// Call PagerWidget's afterRequest() method
			this._super();
			
			// now add a full count to the items we have
			var totalCount = IndexManager.response.response.numFound;
			var totalShown = IndexManager.response.response.docs.length;
			var startIndex = IndexManager.response.response.start;
			
			if(totalCount > 0) {
				var html = 'Showing ' + (startIndex+1) + ' through ' + (startIndex+totalShown) + ' of ' + totalCount;
				
				$('#pager_top_footer').html(html);
			} else {
				$('#pager_top_footer').html('<i>No Results</i>');
			}
		}
	});
})(jQuery);