%% extract pixel data from user defined region
image = Iresc;
mask = roipoly(image);
pixels = regionprops(mask, 'PixelList');
pixeldata = uint8(zeros(size(pixels.PixelList,1),3));
for i=1:size(pixels.PixelList,1),
    pixeldata(i,1) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),1);
    pixeldata(i,2) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),2);
    pixeldata(i,3) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),3);
end

%% extract a and b channel pixel data from L*a*b format picture
image = Iresc;
mask = roipoly(image);
pixels = regionprops(mask, 'PixelList');
pixeldata = uint8(zeros(size(pixels.PixelList,1),2));
for i=1:size(pixels.PixelList,1),
    pixeldata(i,1) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),2);
    pixeldata(i,2) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),3);
end


%% extract gold pixel data
image = Iresc;
mask = roipoly(image);
pixels = regionprops(mask, 'PixelList');
pixeldata = uint8(zeros(size(pixels.PixelList,1),4));
for i=1:size(pixels.PixelList,1),
    pixeldata(i,1) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),1);
    pixeldata(i,2) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),2);
    pixeldata(i,3) = image(pixels.PixelList(i,2),pixels.PixelList(i,1),3);
    pixeldata(i,4) = double(abs(A*pixels.PixelList(i,1)+B*(size(Ith,1)-pixels.PixelList(i,2))+C)/sqrt(A^2+B^2))/mask_props.MajorAxisLength*300.0;
end

%% append pixeldata
pixeldata_old = android_gray_lab_pixeldata;
pixeldata = vertcat(pixeldata_old, pixeldata);

%% export average and covariance matrix
export_data = double(android_white_lab_pixeldata)/255;
mu = mean(export_data);
C = inv(cov(double(export_data)));
dlmwrite('android_white_lab_constants.txt',[mu; C], 'delimiter', ',');

