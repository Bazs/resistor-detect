mu00 = central_moments(Ith,0,0);
mu11 = central_moments(Ith,1,1);
mu20 = central_moments(Ith,2,0);
mu02 = central_moments(Ith,0,2);
mu11p = mu11/mu00;
mu20p = mu20/mu00;
mu02p = mu02/mu00;
orient1 = 0.5*atan(2*mu11p/(mu20p-mu02p))*180/pi;
if (mu11 < 0)
    if (orient1 < 0) orient1 = -orient1;
    else orient1 = 90 - orient1;
    end
else
    if (orient1 > 0) orient1  = -orient1;
    else orient1 = 90 + orient1;
    end
end



props = regionprops(Ith, 'Orientation');
orient_matlab = props.Orientation 
mu11
%%
Ith = imrotate(Ith, 20);
imshow(Ith);