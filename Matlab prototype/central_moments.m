function cmom = central_moments(im,i,j)
    rawm00 = raw_moments(im,0,0);
    centroids = [raw_moments(im,1,0)/rawm00 , raw_moments(im,0,1)/rawm00];
    cmom = sum(sum( (([1:size(im,1)]-centroids(2))'.^j * ...
                     ([1:size(im,2)]-centroids(1)).^i) .* im ));
end