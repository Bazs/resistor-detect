function lab_pixeldata = pixels_RGB2Lab(pixeldata)

lab_pixeldata = zeros(length(pixeldata),3);

for idx=1:length(pixeldata)
[lab_pixeldata(idx,1), lab_pixeldata(idx,2), lab_pixeldata(idx,3)]=RGB2Lab(pixeldata(idx,1), pixeldata(idx,2), pixeldata(idx, 3));
end
lab_pixeldata(:,1) = lab_pixeldata(:,1)*255/100;
lab_pixeldata(:,2) = lab_pixeldata(:,2) + 128;
lab_pixeldata(:,3) = lab_pixeldata(:,3) + 128;
end