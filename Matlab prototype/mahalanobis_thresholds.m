threshold_num = 10;


Ibin = zeros(size(Iresc,1), size(Iresc,2), threshold_num);
for threshold = 0:threshold_num;
    for k=1:size(Iresc,1),
        for l=1:size(Iresc,2),
            distance = mahal(double([Iresc(k,l,1) Iresc(k,l,2) Iresc(k,l,3)]), double(pixeldata));
            if (distance < threshold/10 && sum(Iresc(k,l,:))),
                Ibin(k,l,threshold) = 1;
            end
        end
    end
end
%%
areas = zeros(1,size(Ibin,3));
for idx=1:size(Ibin),
    area(idx)=sum(sum(Ibin(:,:,idx)));
end
plot(area);
%%
largest_area = 0;
largest_areas = zeros(1,length(Ibin));
Ibin_d = Ibin;
str_bands = strel('disk',1,8);
for idx=1:size(Ibin_d),
    Ibin_d(:,:,idx) = imdilate(Ibin(:,:,idx),str_bands);
    Ibin_d(:,:,idx) = imerode(Ibin(:,:,idx),str_bands);
    [L, m] = bwlabel(Ibin_d(:,:,idx),8);
    largest_area = 0;
    for l=1:m,
        Icurr = zeros(size(L));
        Icurr(find(L==l))=1;
        area = sum(sum(Icurr));
        if (area > largest_area)
            largest_area = area;
        end
    end
    largest_areas(idx) = largest_area;
end
plot(largest_areas);