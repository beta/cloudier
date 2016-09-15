package net.kyouko.cloudier.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event for viewing images.
 *
 * @author beta
 */
public class ViewImageEvent {

    public List<String> imageUrls;
    public int startPosition = 0;


    public ViewImageEvent(String imageUrl) {
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(imageUrl);
        this.imageUrls = imageUrls;
        this.startPosition = 0;
    }


    public ViewImageEvent(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        this.startPosition = 0;
    }


    public ViewImageEvent(List<String> imageUrls, int startPosition) {
        this.imageUrls = imageUrls;
        this.startPosition = startPosition;
    }

}
