import sys

if __name__ == "__main__":
    if len(sys.argv) != 7:
        raise ValueError("Number of param should be 6")
    
    input_file_name = sys.argv[1]
    output_file_name = sys.argv[2]
    w_url, w_keywords, w_title, w_body = float(sys.argv[3]), float(sys.argv[4]), float(sys.argv[5]), float(sys.argv[6])
    if w_url + w_keywords + w_title + w_body - 1.0 > 1e-9:
        raise ValueError("sum of weight should be 1")

    
    with open(input_file_name) as f:
        lines = f.readlines()
    with open(output_file_name, 'w') as f:
        fields = [".url", ".keywords", ".title", ".body"]
        weights = [str(w_url), str(w_keywords), str(w_title), str(w_body)]
        for line in lines:
            data = line.split(":")
            id = data[0].strip(" ")
            qry = data[1].strip(" ")
            words = qry.split(" ")
            new_qry = [id, ":", "#AND( "]
            for word in words:
                new_qry.append("#WSUM(")
                for i in range(len(fields)):
                    weight, field = weights[i], fields[i]
                    new_qry.append(weight)
                    new_qry.append(word.strip("\n") + field)
                    
                new_qry.append(")")
            new_qry.append(")")
            output = " ".join(new_qry)
            f.write(output)
            f.write("\n")
            
            
    # 33:#AND( #WSUM(0.1 elliptical.url 0.2 elliptical.title 0.3 elliptical.inlink 0.4 elliptical.body) #WSUM(0.1 trainer.url 0.2 trainer.title 0.3 trainer.inlink 0.4 trainer.body))

        
    
    